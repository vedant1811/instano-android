<!DOCTYPE html>
<html>
<body>

<?php 
$text1=$_GET['text1']; 
$text2 = $_GET['text2']; 

$output1 = "Test output 1";
$output2 = "Test output 2";

$xml = new SimpleXMLElement('<xml/>');
$mydata = $xml->addChild('mydata');
$mydata->addChild('text1',$output1);
$mydata->addChild('text2',$output2);

$fp = fopen("OutputData.xml","wb");
fwrite($fp,$xml->asXML());
fclose($fp);
 
?>

</body>
