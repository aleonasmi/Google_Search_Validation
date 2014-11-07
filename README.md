Google_Search_Validation
========================
About

A framework that can be used to automate Google Search; it executes searches for different keywords
(supplied as command line parameters), validates a link (supplied as command line parameters) and 
compare total amount of the returns with expected (supplied as command line parameters).

Installation

git clone  https://github.com/aleonasmi/Google_Search_Validation

Execution

Change directories to the location of the project you cloned
Execute in command prompt: 
mvn clean test -Dkwd="quality assurance" -DlinkToValidate="http://en.wikipedia.org/wiki/Quality_assurance" -DexpectedAmount="109000000"

Report
File report_01.txt is located in [your_path]\Google_Search_Validation\src\test\reports




