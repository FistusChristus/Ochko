package com.company;// Fig. 27.7: Client.java
// Client portion of a stream-socket connection between client and server.

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Player extends JFrame 
{
	private JButton Hit;
	private JButton Stay;
	private JPanel buttons;
	private JTextArea displayArea; // display information to user
	private ObjectOutputStream output; // output stream to server
	private ObjectInputStream input; // input stream from server
	private String message = ""; // message from server
	private String chatServer; // host server for this application
	private Socket client; // socket to communicate with server
	private int cardamt=0;
	// initialize chatServer and set up GUI
	public Player( String host )
	{
		super( "Игрок" );

		chatServer = host; // set server to which this client connects

		buttons = new JPanel();
		buttons.setLayout(new GridLayout(1,2));
		Hit = new JButton("Тянуть");
		Stay = new JButton("Пас");
		
		Hit.addActionListener(
				new ActionListener() 
				{
					// send message to server
					public void actionPerformed( ActionEvent event )
					{
						sendData( "Тянуть" );
					} // end method actionPerformed
				} // end anonymous inner class
				); // end call to addActionListener
		
		Stay.addActionListener(
				new ActionListener() 
				{
					// send message to server
					public void actionPerformed( ActionEvent event )
					{
						sendData( "Пас" );
					} // end method actionPerformed
				} // end anonymous inner class
				); // end call to addActionListener

		buttons.add(Hit, BorderLayout.SOUTH);
		buttons.add(Stay, BorderLayout.SOUTH);
		buttons.setVisible(true);
		add(buttons,BorderLayout.SOUTH);
		displayArea = new JTextArea(); // create displayArea
		add( new JScrollPane( displayArea ), BorderLayout.CENTER );

		setSize( 300, 300 ); // set size of window
		setVisible( true ); // show window
	} // end Client constructor

	// connect to server and process messages from server
	public void runClient() 
	{
		try // connect to server, get streams, process connection
		{
			connectToServer(); // create a Socket to make connection
			getStreams(); // get the input and output streams
			processConnection(); // process connection
		} // end try
		catch ( EOFException eofException ) 
		{
			displayMessage( "\nКлиент прервал соединение" );
		} // end catch
		catch ( IOException ioException ) 
		{} // end catch
		finally 
		{
			closeConnection(); // close connection
		} // end finally
	} // end method runClient

	// connect to server
	private void connectToServer() throws IOException
	{      
		displayMessage( "Попытка соединения\n" );

		// create Socket to make connection to server
		client = new Socket( InetAddress.getByName( chatServer ), 23555 );

		// display connection information
		displayMessage( "Подключено: " +
				client.getInetAddress().getHostName() );
	} // end method connectToServer

	// get streams to send and receive data
	private void getStreams() throws IOException
	{
		// set up output stream for objects
		output = new ObjectOutputStream( client.getOutputStream() );      
		output.flush(); // flush output buffer to send header information

		// set up input stream for objects
		input = new ObjectInputStream( client.getInputStream() );

		displayMessage( "\nПолучены потоки ввода-вывода\n" );
	} // end method getStreams

	// process connection with server
	private void processConnection() throws IOException
	{


		do // process messages sent from server
		{ 
			try // read message and display it
			{
				message = ( String ) input.readObject(); // read new message
				displayMessage( "\n" + message ); // display message
				if (message.contains("Поражение!") || message.contains("Пожалуйста, подождите...")){
					buttons.setVisible(false);
				}
				
			} // end try
			catch ( ClassNotFoundException classNotFoundException ) 
			{
				displayMessage( "\nПолучен объект неизвестного типа" );
			} // end catch

		} while ( !message.equals( "SERVER>>> TERMINATE" ) );
	} // end method processConnection

	// close streams and socket
	private void closeConnection() 
	{
		displayMessage( "\nЗакрываем соединение" );
		

		try 
		{
			output.close(); // close output stream
			input.close(); // close input stream
			client.close(); // close socket
		} // end try
		catch ( IOException ioException ) 
		{} // end catch
	} // end method closeConnection

	// send message to server
	private void sendData( String message )
	{
		try // send object to server
		{
			output.writeObject(  message );
			output.flush(); // flush data to output
			
		} // end try
		catch ( IOException ioException )
		{
			displayArea.append( "\nОшибка записи объекта" );
		} // end catch
	} // end method sendData

	// manipulates displayArea in the event-dispatch thread
	private void displayMessage( final String messageToDisplay )
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run() // updates displayArea
					{
						displayArea.append( messageToDisplay );
					} // end method run
				}  // end anonymous inner class
				); // end call to SwingUtilities.invokeLater
	} // end method displayMessage


	

	
}//end player class
