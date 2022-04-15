.PHONY: test
test:
	clojure -M:dev:test

.PHONY: lint
lint:
	cljstyle check
	clj-kondo --lint src:test

.PHONY: outdated
outdated:
	clojure -M:outdated

.PHONY: clean
clean:
	rm -rf .cpcache target
