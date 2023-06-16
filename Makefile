.PHONY: test
test:
	clojure -M:dev:test

.PHONY: lint
lint:
	cljstyle check
	clj-kondo --lint src:test

.PHONY: install
install: clean
	clojure -T:build install

.PHONY: outdated
outdated:
	clojure -M:outdated --upgrade

.PHONY: clean
clean:
	rm -rf .cpcache target

.PHONY: import-clj-kondo-config
import-clj-kondo-config:
	@rm -rf .clj-kondo/.cache
	@clj-kondo --copy-configs --dependencies --lint "$(shell clojure -A:dev -Spath)"
