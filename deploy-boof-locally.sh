#!/bin/bash
lein jar
lein pom
lein localrepo install -p pom.xml target/provided/eye-boof-0.1.0-SNAPSHOT.jar org.clojars.boechat107/eye-boof "0.1.0-SNAPSHOT"
