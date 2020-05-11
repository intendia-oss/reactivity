if [ "$TRAVIS_REPO_SLUG" == "intendia-oss/reactivity" ] && \
   [ "$TRAVIS_JDK_VERSION" == "openjdk11" ] && \
   [ "$TRAVIS_PULL_REQUEST" == "false" ] && \
   [ "$TRAVIS_BRANCH" == "master" ]; then

  mvn -s ci/settings.xml clean source:jar deploy -Dmaven.test.skip=true -Dinvoker.skip=true
fi
