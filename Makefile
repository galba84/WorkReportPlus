.PHONY: build demo stop test frontend
build:
	bash gradlew build
demo: build
	docker compose up --build --wait
stop:
	docker compose down
test:
	bash gradlew test
frontend:
	cd frontend && npm ci && npm run build
