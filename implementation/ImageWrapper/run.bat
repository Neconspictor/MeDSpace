Pushd "%~dp0"
Pushd "../"
:java -cp "lib/*" play.core.server.ProdServerStart
.\bin\sqlwrapper
popd
popd