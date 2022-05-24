<?php
$text = escapeshellarg($_POST['text']);

$output = shell_exec("echo $text | dot -Tsvg");

echo $output;
?>