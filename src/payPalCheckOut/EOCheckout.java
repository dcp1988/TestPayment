package payPalCheckOut;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Servlet implementation class EOCheckout
 */
@WebServlet("/EOCheckout")
public class EOCheckout extends HttpServlet {
	private static final long serialVersionUID = 1L;



	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try{
			String amount = request.getParameter("amount");
			String currency = request.getParameter("currency");
			if(amount !=null){
				System.out.println("amount : "+ amount);
				System.out.println("currency : "+ currency);
				String authKey = getAuthToken(amount,currency,response);
				System.out.println("authKey : "+ authKey);
				if(authKey != null){
					FileWriter aAuthWriter = new FileWriter("auth.txt"); 
					aAuthWriter.write(authKey);
					aAuthWriter.close();
					System.out.println("file created susccesfully : ");
					createOrders(amount,currency,authKey,response);
				}else{
					System.out.println("amount : "+ amount);
					response.sendRedirect("Oncancel.html");
				}
			}else{
				System.out.println("amount : "+ amount);
				response.sendRedirect("Oncancel.html");
			}
		}
		catch(Exception e){
			System.out.println("Error : "+ e.getMessage());
			response.sendRedirect("Oncancel.html");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	public static String getAuthToken(String amount, String currency ,HttpServletResponse response) throws IOException{

		URL url = new URL("https://api.sandbox.paypal.com/v1/oauth2/token");
		String authKey = null;
		// Open a connection(?) on the URL(?) and cast the response(??)
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		String postData = "grant_type=client_credentials";
		//String encoded = Base64.getEncoder().encodeToString(("ASiBqjtWQfyof8RWRgUiknB9w96p4G48sFoa73NRuKhp0d_s1IdX6v1SbjmnLFlb2GPUPOJvBGat2kUe"+":"+"EGTVfPKKnWplgzYM86LbNW1IkqYIsLlJdHSK8n8tP5MPoEh4GNe8n_HFdgk2YTrDFBExeaXBSTAer9mQ").getBytes(StandardCharsets.UTF_8));  //Java 8
		//connection.setRequestProperty("Authorization", "Basic "+encoded);
		// Now it's "open", we can set the request method, headers etc.
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Accept-Language", "en_US");
		connection.setRequestProperty("Authorization", "Basic QVNpQnFqdFdRZnlvZjhSV1JnVWlrbkI5dzk2cDRHNDhzRm9hNzNOUnVLaHAwZF9zMUlkWDZ2MVNiam1uTEZsYjJHUFVQT0p2QkdhdDJrVWU6RUdUVmZQS0tuV3BsZ3pZTTg2TGJOVzFJa3FZSXNMbEpkSFNLOG44dFA1TVBvRWg0R05lOG5fSEZkZ2syWVRyREZCRXhlYVhCU1RBZXI5bVE=");
		connection.setDoOutput(true);
		connection.connect();
		byte[] outputBytes = postData.getBytes("UTF-8");
		OutputStream out = connection.getOutputStream();
		out.write(outputBytes);
		out.flush();
		out.close();

		int statusCode = connection.getResponseCode();
		System.out.println("POST Response Code :: " + statusCode);
		if (statusCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;
			StringBuffer responseString = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				responseString.append(inputLine);
			}
			in.close();
			System.out.println("authentication response " +responseString);
			JSONObject jsonAuth = new JSONObject(responseString.toString());
			String authToken = jsonAuth.getString("access_token");
			authKey = authToken; 
			System.out.println("authentication jsonAuth response " +jsonAuth);
			System.out.println("authentication jsonAuth authToken " +authToken);
			// print result
			
		} else {
			System.out.println("POST request not worked");
			response.sendRedirect("Oncancel.html");
		}
		return authKey;
	}
	public static void createOrders(String amount, String currency,String authKey,HttpServletResponse response) throws IOException{

		URL url = new URL("https://api.sandbox.paypal.com/v2/checkout/orders");
		String redirectUrlPayPal = null;
		System.out.println("createOrders authKey :: " + authKey);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		String postData = "{\"intent\": \"CAPTURE\",\"application_context\":{\"return_url\": \"http://localhost:8080/CheckOut/return.jsp\", \"cancel_url\": \"http://localhost:8080/CheckOut/Oncancel.html\",\"payment_method\": { \"payer_selected\": \"PAYPAL\" } }, \"purchase_units\": [{\"amount\":{\"currency_code\": \"USD\",\"value\": \"2.00\"}}]}";
		System.out.println("createOrders postData :: " + postData);
		// Now it's "open", we can set the request method, headers etc.
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Authorization", "Bearer "+authKey+"");
		connection.connect();
		byte[] outputBytes = postData.getBytes("UTF-8");
		OutputStream out = connection.getOutputStream();
		out.write(outputBytes);
		out.flush();
		out.close();

		int statusCode = connection.getResponseCode();
		System.out.println("createOrders Response Code :: " + statusCode);
		if (statusCode == HttpURLConnection.HTTP_CREATED) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;
			StringBuffer responseOrder = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				responseOrder.append(inputLine);
			}
			in.close();
			System.out.println("createOrders response " +responseOrder);
			JSONObject jsonResp = new JSONObject(responseOrder.toString());
			System.out.println("createOrders redirectURLs array" +jsonResp.getJSONArray("links"));
			JSONArray redirectURLs = jsonResp.getJSONArray("links");
			redirectURLs.iterator();
			System.out.println("createOrders redirectURLs " +redirectURLs);
			for(int i=0; i<redirectURLs.length();i++){
				JSONObject explrObject = redirectURLs.getJSONObject(i);  
				System.out.println("createOrders explrObject " +explrObject);
				if(explrObject.getString("rel").equals("approve")){
					redirectUrlPayPal = explrObject.getString("href");
					System.out.println("createOrders redirectUrlPayPal " +redirectUrlPayPal);
					break;
				}
			}
			if(redirectUrlPayPal != null){
				System.out.println("createOrders approve URL " +redirectUrlPayPal);
				response.sendRedirect(redirectUrlPayPal);
			}else{
				System.out.println("POST request not worked");
				response.sendRedirect("Oncancel.html");
			}
			
		} else {
			System.out.println("POST request not worked");
			response.sendRedirect("Oncancel.html");
		}
	}
}
