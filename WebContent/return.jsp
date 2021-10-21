<%@page import="java.util.Locale.Category"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.net.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.text.ParseException"%>
<%@ page import="java.util.Map.Entry"%>
<%
	
	boolean refresh = false;
	boolean exception = false;
	String tMessage = "Success";
	String refMessage = "Your transaction is successful.";
	String order_id = request.getParameter("token");
	System.out.println("capture order_id :: " + order_id);
	String paypal_id = request.getParameter("PayerID");
	System.out.println("capture paypal_id :: " + paypal_id);
	String url  = "https://api.sandbox.paypal.com/v2/checkout/orders/"+order_id+"/capture";
	System.out.println("capture url original:: " + url);
	//url.replace("orderid", order_id);
	//System.out.println("capture url after replace :: " + url);
	BufferedReader fileIn = new BufferedReader(new FileReader("auth.txt"));
	String line;
	String authToken = null;
	while((line = fileIn.readLine()) != null){
		System.out.println("line in txt" + line);
		authToken = line;
	}
	fileIn.close();
	System.out.println("authToken in txt final" + authToken);
	String postData = "";
	
	URL urlCapture = new URL(url);
	DataOutputStream printout;
	DataInputStream  input;
	HttpURLConnection httpConn = (HttpURLConnection)urlCapture.openConnection();
	httpConn.setDoOutput( true );
	httpConn.setRequestMethod("POST");
	httpConn.setRequestProperty("Content-Type", "application/json");
	httpConn.setRequestProperty("Authorization", "Bearer "+authToken+"");
	httpConn.setRequestProperty("PayPal-Request-Id", paypal_id);
	httpConn.connect();
	byte[] outputBytes = postData.getBytes("UTF-8");
	OutputStream output = httpConn.getOutputStream();
	output.write(outputBytes);
	output.flush();
	output.close();
	
	int responseCode = httpConn.getResponseCode();
	System.out.println("capture responseCode:: " + responseCode);
	if(responseCode == 200 || responseCode == 201){
		BufferedReader in = new BufferedReader(new InputStreamReader(
				httpConn.getInputStream()));
		String inputLine;
		StringBuffer responseOrder = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			responseOrder.append(inputLine);
		}
		in.close();
		System.out.println("captureOrders response " +responseOrder);
		JSONObject jsonResp = new JSONObject(responseOrder.toString());
		System.out.println("captureOrders response jsonResp" +jsonResp);
	}else{
		tMessage = "Failure";
		refMessage = "Your transaction has failed";
	}
	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Payment Response</title>
</head>
<body>
	<div class='line'><%=tMessage%><br/></div>
	<div class='line'><%=refMessage%></div>
</body>
</html>
