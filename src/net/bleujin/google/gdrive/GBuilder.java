package net.bleujin.google.gdrive;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.common.collect.Lists;

public class GBuilder {

	private String applicationName = "CrakenDrive";
	private String tokenPath = "./resource/apikey";

	private String credentialFile = "./resource/apikey/credentials.json";

	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final List<String> SCOPES = Lists.newArrayList(DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE_FILE, Oauth2Scopes.USERINFO_PROFILE);

	private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, String user) throws IOException {
		// Load client secrets.
		InputStream in = new FileInputStream(credentialFile);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + credentialFile);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokenPath)))
//				.addRefreshListener(new CredentialRefreshListener() {
//					@Override
//					public void onTokenResponse(Credential arg0, TokenResponse arg1) throws IOException {
//					}
//					
//					@Override
//					public void onTokenErrorResponse(Credential arg0, TokenErrorResponse arg1) throws IOException {
//					}
//				})
				// .setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance())
				.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize(user);
		
		// returns an authorized Credential object.
		return credential;
	}

	public GBuilder applicationName(String applicationName) {
		this.applicationName = applicationName ;
		return this ;
	}
	
	public GBuilder tokenPath(String tokenPath) {
		this.tokenPath = tokenPath ;
		return this ;
	}
	
	public GBuilder credentialFile(String credentialFile) {
		this.credentialFile = credentialFile ;
		return this ;
	}

	public static GBuilder create() {
		return new GBuilder();
	}

	public GFile root(String user) throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, user)).setApplicationName(applicationName).build();
		
		return new GFile(new GDrive(service), service.files().get("root").execute(), false) ;
	}
}
