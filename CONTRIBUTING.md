# Contributing

Thanks for your interest in improving Steam Widget! This project is small, so the process is kept lightweight.

Please also read our [Code of Conduct](CODE_OF_CONDUCT.md) — it applies to all interactions in this repository.

---

## Before You Start

- For bugs or feature ideas, please [open an issue](https://github.com/felix-sharky/steam-widget/issues/new/choose) first using the appropriate template (bug report, feature request, or question) — especially for larger changes, so we can agree on the approach before you invest time in it.
- Check existing issues and pull requests to avoid duplicate work.

## Setting Up Your Environment

See [DEVELOPMENT.md](DEVELOPMENT.md) for prerequisites, configuration, and instructions to build and run the project locally.

## Making Changes

1. Fork the repository and create a branch off `main` with a short, descriptive name (e.g. `fix/vanity-url-nullpointer`, `feature/pastel-theme`).
2. Keep changes focused — unrelated fixes or refactors should go in a separate PR.
3. Follow the existing code style and package structure (see the "Project Layout" section in [DEVELOPMENT.md](DEVELOPMENT.md)).
4. Add or update tests where practical, and make sure the full test suite passes:
   ```bash
   ./mvnw test
   ```
5. If you change or add an API endpoint, update [API.md](API.md) accordingly.
6. Never commit secrets (Steam API keys, database credentials) — configuration should be supplied via environment variables, not committed into `application.properties`.

## Submitting a Pull Request

1. Push your branch and open a pull request against `main`.
2. Write a clear description of *what* changed and *why*, and link any related issue.
3. Be responsive to review feedback — small, iterative fixes are easier to review than a single large rewrite.
4. Once approved, a maintainer will merge the PR.

## Reporting Security Issues

Please do not open a public issue for security vulnerabilities. See [SECURITY.md](SECURITY.md) if present, or otherwise contact the maintainers directly through [sharky.codes](https://sharky.codes).

## License

By contributing, you agree that your contributions will be licensed under the project's [Apache License 2.0](LICENSE).
