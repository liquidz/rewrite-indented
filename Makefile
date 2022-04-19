.PHONY: test
test:
	clojure -M:dev:test

.PHONY: lint
lint:
	cljstyle check
	clj-kondo --lint src:test

.PHONY: install
install:
	clojure -T:build install

.PHONY: deploy
deploy:
	clojure -T:build deploy

.PHONY: outdated
outdated:
	clojure -M:outdated

.PHONY: clean
clean:
	rm -rf .cpcache target
