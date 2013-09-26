rmdir report
rmdir coverage
cd bin
cls
java -jar JsTestDriver.jar --port 42442 --browser "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe" --tests all --runnerMode QUIET --testOutput ../results --config JsTestDriver.conf
java -jar jgenhtml.jar  "../results/jsTestDriver.conf-coverage.dat" --output-directory "../coverage" --config-file jgenhtml.lcovrc
cd ..