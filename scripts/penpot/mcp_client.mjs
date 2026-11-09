#!/usr/bin/env node

import { createHash } from "node:crypto";
import { mkdir, readFile, rm, stat, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const expectedTools = ["high_level_overview", "penpot_api_info", "export_shape"];
const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const exportRoot = path.join(repoRoot, "docs/design/exports");
const manifestPath = path.join(exportRoot, "EXPORT_MANIFEST.json");

function usage() {
  console.error("Usage: mcp_client.mjs <check|export-home|import-image> <mcp-url> [args]");
  console.error("  export-home ... --shape <shape-id>:<asset-name>");
  console.error("  import-image ... <absolute-png-path> [--x n --y n --width n --height n]");
  process.exit(2);
}

function requestBody(id, method, params) {
  return JSON.stringify({ jsonrpc: "2.0", id, method, params });
}

async function post(url, sessionId, body) {
  const response = await fetch(url, {
    method: "POST",
    headers: {
      Accept: "application/json, text/event-stream",
      "Content-Type": "application/json",
      ...(sessionId ? { "Mcp-Session-Id": sessionId } : {})
    },
    body
  });
  const raw = await response.text();
  if (!response.ok) throw new Error(`MCP request failed (${response.status}): ${raw}`);
  if (!raw.trim()) return { sessionId: response.headers.get("mcp-session-id") ?? sessionId, payload: null };
  const jsonLine = raw.split("\n").find((line) => line.startsWith("data:"));
  const payload = jsonLine ? jsonLine.slice(5).trim() : raw;
  return { sessionId: response.headers.get("mcp-session-id") ?? sessionId, payload: JSON.parse(payload) };
}

async function connect(url) {
  const initialized = await post(url, null, requestBody(1, "initialize", {
    protocolVersion: "2025-03-26",
    capabilities: {},
    clientInfo: { name: "crackmydroid-penpot-export", version: "1.0.0" }
  }));
  if (!initialized.payload) throw new Error(`MCP initialization at ${url} returned an empty response.`);
  if (initialized.payload.error) throw new Error(`MCP initialization error: ${initialized.payload.error.message}`);
  if (!initialized.sessionId) throw new Error("MCP server did not return Mcp-Session-Id.");
  await post(url, initialized.sessionId, JSON.stringify({ jsonrpc: "2.0", method: "notifications/initialized" }));
  return initialized.sessionId;
}

async function listTools(url, sessionId) {
  const response = await post(url, sessionId, requestBody(2, "tools/list", {}));
  if (response.payload.error) throw new Error(`tools/list failed: ${response.payload.error.message}`);
  return response.payload.result.tools;
}

async function callTool(url, sessionId, name, argumentsValue) {
  const response = await post(url, sessionId, requestBody(3, "tools/call", { name, arguments: argumentsValue }));
  if (!response.payload) throw new Error(`${name} returned an empty response from ${url}.`);
  if (response.payload.error) throw new Error(`${name} failed: ${response.payload.error.message}`);
  const result = response.payload.result;
  const detail = result?.content?.map((entry) => entry.text ?? entry.type).join("; ") ?? "";
  if (!result || result.isError || /Tool execution failed|No Penpot plugin instances are currently connected/i.test(detail)) {
    const suffix = detail || "no tool result returned";
    throw new Error(`${name} reported an error at ${url}: ${suffix}. Open CrackMyDroid, reload the localhost manifest, and click Connect to MCP server.`);
  }
  return result;
}

function parseShapes(argumentsList) {
  const shapes = [];
  for (let index = 0; index < argumentsList.length; index += 1) {
    if (argumentsList[index] !== "--shape") continue;
    const value = argumentsList[index + 1];
    if (!value?.includes(":")) throw new Error("Each --shape needs <shape-id>:<asset-name>.");
    const [id, ...nameParts] = value.split(":");
    shapes.push({ id, name: nameParts.join(":").replace(/[^a-zA-Z0-9._-]/g, "-") });
    index += 1;
  }
  return shapes;
}

async function sha256(filePath) {
  return createHash("sha256").update(await readFile(filePath)).digest("hex");
}

async function writeManifest(exportedAssets) {
  const entries = await Promise.all(exportedAssets.map(async (asset) => ({
    ...asset,
    sha256: await sha256(path.join(exportRoot, asset.path))
  })));
  const manifest = {
    schemaVersion: 1,
    source: { application: "Penpot local", file: "CrackMyDroid", page: "Home" },
    generatedAt: new Date().toISOString(),
    tokens: "design-tokens.json",
    assets: entries,
    codeTargets: ["HomeContent", "SectionHeader", "AppCard", "AppAccordion", "PackageIcon"]
  };
  await writeFile(manifestPath, `${JSON.stringify(manifest, null, 2)}\n`);
}

async function check(url) {
  const sessionId = await connect(url);
  const tools = await listTools(url, sessionId);
  const names = tools.map((tool) => tool.name);
  const missing = expectedTools.filter((name) => !names.includes(name));
  if (missing.length) throw new Error(`MCP is running but missing tools: ${missing.join(", ")}`);
  console.log(`OK MCP server exposes: ${expectedTools.join(", ")}`);
  try {
    await callTool(url, sessionId, "export_shape", { shapeId: "page", format: "svg", mode: "shape" });
    console.log("OK Penpot plugin is connected to an active design page");
  } catch (error) {
    throw new Error(`MCP is reachable, but the active Penpot plugin/page is unavailable. Open CrackMyDroid, load the local plugin and click Connect. ${error.message}`);
  }
}

async function exportHome(url, argumentList) {
  const shapes = parseShapes(argumentList);
  if (!shapes.length) throw new Error("No assets requested. Pass --shape <shape-id>:<asset-name> for every Home SVG to export.");
  const sessionId = await connect(url);
  const tools = await listTools(url, sessionId);
  if (!tools.some((tool) => tool.name === "export_shape")) throw new Error("MCP server does not expose export_shape.");
  await mkdir(path.join(exportRoot, "screens"), { recursive: true });
  const exportedAssets = [];
  for (const shape of shapes) {
    const outputPath = path.join(exportRoot, "screens", `${shape.name}.svg`);
    await rm(outputPath, { force: true });
    await callTool(url, sessionId, "export_shape", {
      shapeId: shape.id,
      format: "svg",
      mode: "shape",
      filePath: outputPath
    });
    await stat(outputPath);
    exportedAssets.push({ page: "Home", shapeId: shape.id, name: shape.name, variant: "default", path: path.relative(exportRoot, outputPath) });
  }
  await writeManifest(exportedAssets);
  console.log(`Exported ${exportedAssets.length} Home SVG asset(s) and updated ${path.relative(repoRoot, manifestPath)}.`);
}

function parseNumberOption(argumentsList, name, positive = false) {
  const index = argumentsList.indexOf(name);
  if (index < 0) return undefined;
  const value = Number(argumentsList[index + 1]);
  if (!Number.isFinite(value) || (positive && value <= 0)) {
    throw new Error(`${name} requires a ${positive ? "positive " : "finite "}number.`);
  }
  return value;
}

async function importImage(url, argumentList) {
  const filePath = argumentList.find((value) => !value.startsWith("--"));
  if (!filePath || !path.isAbsolute(filePath)) {
    throw new Error("import-image requires an absolute PNG/JPEG/GIF/WEBP path.");
  }
  await stat(filePath).catch(() => {
    throw new Error(`Image file does not exist or is not readable: ${filePath}`);
  });
  const sessionId = await connect(url);
  const tools = await listTools(url, sessionId);
  if (!tools.some((tool) => tool.name === "import_image")) {
    throw new Error("MCP server does not expose import_image.");
  }
  const result = await callTool(url, sessionId, "import_image", {
    filePath,
    x: parseNumberOption(argumentList, "--x"),
    y: parseNumberOption(argumentList, "--y"),
    width: parseNumberOption(argumentList, "--width", true),
    height: parseNumberOption(argumentList, "--height", true)
  });
  const text = result.content?.map((entry) => entry.text ?? entry.type).join("; ") ?? "image imported";
  console.log(`Imported ${filePath}: ${text}`);
}

const [command, url, ...argumentsList] = process.argv.slice(2);
if (!command || !url || !["check", "export-home", "import-image"].includes(command)) usage();

try {
  if (command === "check") await check(url);
  else if (command === "export-home") await exportHome(url, argumentsList);
  else await importImage(url, argumentsList);
} catch (error) {
  console.error(`Error: ${error.message}`);
  process.exit(1);
}
