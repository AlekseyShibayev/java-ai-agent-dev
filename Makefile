GRADLE_USER_HOME := /tmp/opencode/gradle-home
GRADLEW := GRADLE_USER_HOME=$(GRADLE_USER_HOME) ./gradlew

.DEFAULT_GOAL := build
.PHONY: build test

# Clean build without running tests.
build:
	$(GRADLEW) clean build -x test

# Run the full test suite.
test:
	$(GRADLEW) test
