.PHONY: help
help:
	@grep -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	 sort | \
	 awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.PHONY: test ## Run the tests once
	clojure -M:test

.PHONY: test-refresh
test-refresh: ## Start test-refresh
	clojure -M:test-refresh
