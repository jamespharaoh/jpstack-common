<html><body>
<?php

$submit = $_GET['submit'];
$month = $_GET['month'];
$year = $_GET['year'];

if ($submit == '1'){

	$dbhost="localhost";
	$dbuser="james";
	$dbpassword="james";
	$db="txt2";

	$conn = pg_connect("host=$dbhost port=5432 dbname=$db user=$dbuser password=$dbpassword");
	//connect to a database named "mary" on the host "sheep" with a username and password

	$result = pg_query($conn, "select CUC.id,CU.code,U.username,CUC.timestamp,CUC.amount,CUC.bill_amount,CUC.details
	from chat_user_credit as CUC
	inner join chat_user as CU on CUC.chat_user_id = CU.id
	inner join \"user\" as U on CUC.user_id = U.id
	where CUC.timestamp::text like '$year-$month-%'::text
	order by CUC.id desc");
	if (!$result) {
		echo "An error occured.\n";
		exit;
	}
	echo "<table border='1'>";
	echo "<tr><th>id</th>";
	echo "<th>code</th>";
	echo "<th>timestamp</th>";
	echo "<th>Credit</th>";
	echo "<th>Bill</th>";
	echo "<th>Details</th>";
	echo "<th>User</th></tr>";
	while ($row = pg_fetch_row($result)) {
		$amount = number_format(($row[4] / 100),2);
		$bill = number_format(($row[5] / 100),2);
		echo "<tr><td>$row[0]</td>";
		echo "<td>$row[1]</td>";
		echo "<td>$row[3]</td>";
		echo "<td>$amount</td>";
		echo "<td>$bill</td>";
		echo "<td>$row[6]</td>";
		echo "<td>$row[2]</td></tr>";
	}
	echo "</table>";

} else if ($submit == '2') {

	$dbhost="localhost";
	$dbuser="james";
	$dbpassword="james";
	$db="txt2";

	$conn = pg_connect("host=$dbhost port=5432 dbname=$db user=$dbuser password=$dbpassword");
	//connect to a database named "mary" on the host "sheep" with a username and password

	$result = pg_query($conn, "select CUC.id,CU.code,CO.username,CUC.timestamp,CUC.amount,CUC.bill_amount,CUC.details
	from chat_user_credit as CUC
	inner join chat_user as CU on CUC.chat_user_id = CU.id
	inner join conuser as CO on CUC.user_id = CO.conuserid
	order by CUC.id desc");
	if (!$result) {
		echo "An error occured.\n";
		exit;
	}
	echo "<table border='1'>";
	echo "<tr><th>id</th>";
	echo "<th>code</th>";
	echo "<th>timestamp</th>";
	echo "<th>Credit</th>";
	echo "<th>Bill</th>";
	echo "<th>Details</th>";
	echo "<th>User</th></tr>";
	while ($row = pg_fetch_row($result)) {
		$amount = number_format(($row[4] / 100),2);
		$bill = number_format(($row[5] / 100),2);
		echo "<tr><td>$row[0]</td>";
		echo "<td>$row[1]</td>";
		echo "<td>$row[3]</td>";
		echo "<td>$amount</td>";
		echo "<td>$bill</td>";
		echo "<td>$row[6]</td>";
		echo "<td>$row[2]</td></tr>";
	}
	echo "</table>";

} else {
	echo "<form method='get' action='credit.php'>
			<input type='hidden' name='submit' value='1'>
			Month - two digits, eg 04<br><input type='text' name='month'><br>
			Year - four digits. eg 2006<br><input type='text' name='year'><br>
			<p><input type='submit' value='submit'></p>
		</form>
		<form method='get' action='credit.php'>
			<input type='hidden' name='submit' value='2'>
			<input type='submit' value='Show All'>
		</form>
";
}
?>
</body></html>
