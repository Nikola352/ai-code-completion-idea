# Cache strategy

## Algorithm picking criteria

There are several possible cache performance metrics that we need to consider and prioritize:

- **Cache hits** - the number of times we hit the cache should be as high as possible to avoid unnecessary LLM prompts
- **Latency** - the cache should return results as fast as possible to minimize delay
- **Resource consumption** - ideally, we would consume as little memory/cpu/disk as possible.

Since the LLM prompts are *very* expensive, the priority should be to minimize the number of LLM calls, and thus we should **prioritize cache hit percentage**.

To manage heavy typing loads, we should also prioritize latency over memory savings.

## Proposed solution

In order to increase the number of cache hits, we will not only return completions for already requested prefixes, but also provide partial matching.

For example, if the user typed `println("` and the LLM returned `println("Hello world!")` we want to return the same suggestion if a user types `println("Hello`.

For this, we will use a **trie** data structure to save code prefixes as keys and LLM completions in node values. This allows reusing the same prefix in a multiple different queries, while maintaining relevance.

## Cache eviction strategy

To prevent excessive memory consumption, the cache must have a fixed maximum capacity. When selecting an eviction strategy, we should account for typical coding patterns. Observations suggest that:

- *Recently used* completions are more likely to be relevant.
- *Longer* completions are more valuable because they provide greater context and represent more expensive LLM queries.

Thus, I propose a **modified Least Recently Used (LRU)** strategy. Instead of always evicting the least recently used entry, we:

- Select a pool (e.g., 10%) of the least recently used entries.
- Evict the shortest completion in this pool.

This prioritizes longer prefixes while retaining the core benefits of LRU caching.

## Semantic hashing

In order to further increase the number of cache hits, we may try to answer to similar prefixes from cache, instead of just exact matches, **text embeddings** are a typical way to implement this. However, they are very computationally intensive and probably not worth adding.

A lighter alternative, such as **SimHash**, could be a feasible compromise.
