# IntelliJ AI Code Completion Plugin

[![Kotlin](https://img.shields.io/badge/kotlin-2.1.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![IntelliJ Platform](https://img.shields.io/badge/IntelliJ_Platform-2024.2-orange.svg)](https://www.jetbrains.org)

An IntelliJ plugin that provides AI-powered code completions using locally running Ollama LLM with an efficient caching system.

## Features

- Real-time code suggestions using local Ollama LLM
- Intelligent prefix-based caching system for performance optimization
- Inline completion rendering using IntelliJ's Inline Completion API
- Support for multiple programming languages

## Requirements

- IntelliJ IDEA 2023.1 or newer
- Ollama installed locally with the `codellama:7b-code` model


## Setup Instructions

### Step 1: Install Ollama
1. Download and install [Ollama](https://ollama.ai/download) for your operating system
2. Pull the CodeLlama model:
```sh
ollama pull codellama:7b-code
```
3. Ensure Ollama server is running:
```sh
ollama serve
```

### Step 2: Install the Plugin

#### Option 1: Install from ZIP

1. Download the ZIP archive from the [Releases section](https://github.com/Nikola352/ai-code-completion-idea/releases)
2. In IntelliJ IDEA, go to:
  - `Settings/Preferences` → `Plugins` → ⚙️ → `Install Plugin from Disk...`
3. Select the downloaded ZIP file
4. Restart the IDE

#### Option 2: Run from Source

- Clone the repository
- Open the project in IntelliJ IDEA
- Run the plugin using ./gradlew runIde

## Usage

Once installed and with Ollama running locally:

1. Open any code file in IntelliJ IDEA
2. Start typing code as usual
3. The plugin will automatically suggest completions that appear as inline gray text
4. Press Tab to accept the suggestion, or continue typing to ignore it

## How it works

## Ollama Integration

The plugin connects to your locally running Ollama server to generate code completions based on the current file context. It sends the code prefix (everything before the cursor) to the Ollama API and displays the generated completion.

## Caching Strategy

The plugin implements an efficient trie-based caching system that:

- Stores prefixes and their completions in a trie data structure
- Enables partial matching so shorter prefixes can reuse results of longer queries
- Employs a modified LRU eviction strategy that:
- Identifies the least recently used completions (bottom 10%)
  - Evicts the shortest completion from this pool
  - Prioritizes retention of longer, more valuable completions

This approach maximizes cache hits while maintaining reasonable memory usage, significantly reducing the number of API calls to the LLM.

## Roadmap

Future improvements planned for this plugin:

- Settings menu for configuring options (model selection, temperature, etc.)
- Status bar indicator showing plugin status and completion statistics
- SimHash-based backup cache strategy for approximate matching
- Support for more Ollama models and customization options

## Feedback and Contributions

Feedback and contributions are welcome! Please feel free to submit issues or pull requests on the GitHub repository.

## License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/Nikola352/ai-code-completion-idea/blob/main/LICENSE) file for details.
