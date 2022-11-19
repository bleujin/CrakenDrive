package net.bleujin.google;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.common.collect.Lists;

import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

public class TestApi {

	/**
	 * Application name.
	 */
	private static final String APPLICATION_NAME = "CrakenDrive";
	/**
	 * Global instance of the JSON factory.
	 */
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	/**
	 * Directory to store authorization tokens for this application.
	 */
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Lists.newArrayList(DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE_FILE, Oauth2Scopes.USERINFO_PROFILE, PeopleServiceScopes.CONTACTS_READONLY,
			PeopleServiceScopes.USERINFO_EMAIL);
	// ListUtil.toList(PeopleServiceScopes.all().toArray(new String[0])) ;
	private static final String CREDENTIALS_FILE_PATH = "./resource/apikey/credentials.json";

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
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
		Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("bleujin");
		
		
		// returns an authorized Credential object.
		return credential;
	}

	@Test
	public void listFile() throws IOException, GeneralSecurityException {
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();

		// Print the names and IDs for up to 10 files.
				
		FileList result = service.files().list().setFields("nextPageToken, files(id, name)").execute();
		List<File> files = result.getFiles();
		if (files == null || files.isEmpty()) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:");
			for (File file : files) {
				System.out.printf("%s (%s)\n", file.getName(), file.getId());
			}
		}
	}

	@Test
	public void uploadFile() throws IOException, GeneralSecurityException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();

		
		// Upload file photo.jpg on drive.
		File fileMetadata = new File();
		fileMetadata.setName("photo.jpg");
		// File's content.
		java.io.File filePath = new java.io.File("./resource/6.jpg");
		// Specify media type and file-path for file.
		FileContent mediaContent = new FileContent("image/jpeg", filePath);
		try {
			File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
			System.out.println("File ID: " + file.getId());
		} catch (GoogleJsonResponseException e) {
			// TODO(developer) - handle error appropriately
			System.err.println("Unable to upload file: " + e.getDetails());
			throw e;
		}
	}


	@Test
	public void viewPeople() throws Exception {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		PeopleService service = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();

		// Request 10 connections.
		ListConnectionsResponse response = service.people().connections().list("people/me").setPageSize(100).setPersonFields("names,emailAddresses").execute();

		// Print display name of connections if available.
		List<Person> connections = response.getConnections();
		if (connections != null && connections.size() > 0) {
			for (Person person : connections) {
				List<Name> names = person.getNames();
				if (names != null && names.size() > 0) {
					Debug.debug("Name: " + person.getNames().get(0).getDisplayName(), person.getEmailAddresses());
//					person.entrySet().stream().forEach(entry -> {
//						if (entry.getValue() != null) Debug.debug(entry.getKey(), entry.getValue()) ;	
//					}) ;
				} else {
					System.out.println("No names available for connection.");
				}
			}
		} else {
			System.out.println("No connections found.");
		}
	}

	@Test
	public void viewToken() throws IOException {
		FileInputStream fi = new FileInputStream(new java.io.File("./tokens/StoredCredential"));
		byte[] barray = IOUtil.toByteArrayWithClose(fi);

		Map<String, byte[]> map = (Map<String, byte[]>) SerializationUtils.deserialize(barray);

		map.entrySet().stream().forEach(entry -> {
			StoredCredential cre = (StoredCredential) SerializationUtils.deserialize(entry.getValue());
			Debug.line(entry.getKey(), cre.getAccessToken(), cre);
		});

	}
	
	@Test
	public void viewProfile() throws Exception {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Oauth2 oauth2 = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
		
		Userinfo uinfo = oauth2.userinfo().get().execute() ;
		Tokeninfo tinfo = oauth2.tokeninfo().execute() ;
		
		Debug.line(uinfo, uinfo.getGender(), tinfo);
	}

}
