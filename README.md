# Orion AccessTransformer

Simple minimal Java Access Transformer written using OW2 ASM & Java 8

## Features
- Follows [FML Access Transformer file specification](https://github.com/MinecraftForge/AccessTransformers/blob/423735c5095eea78059f96f1a9167cf423b32e12/FMLAT.md)
- Really simple and no-bullshit API - simply load your AT lines and start transforming classes
- Does not call `System.out.print*` unlike similar libraries - making it suitable for embedding into
various projects
- Depends only on OW2 ASM and Java 8, optionally on SLF4J logging to support debugging.

## License

MIT
