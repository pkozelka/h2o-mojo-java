all: build

build:
	mvn clean verify

run:
	java -jar target/h2o-mojo-java.jar
