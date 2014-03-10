package com.phinmadvader.andcpp;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.phinvader.libjdcpp.DCClient;
import com.phinvader.libjdcpp.DCCommand;
import com.phinvader.libjdcpp.DCConstants;
import com.phinvader.libjdcpp.DCDownloader;
import com.phinvader.libjdcpp.DCFileList;
import com.phinvader.libjdcpp.DCMessage;
import com.phinvader.libjdcpp.DCPreferences;
import com.phinvader.libjdcpp.DCUser;

/**
 * Created by phinfinity on 11/08/13.
 */
public class DCPPService extends IntentService {
	public static enum DCClientStatus {
		DISCONNECTED, INVALIDIP, CONNECTED
	}

	public static String[] FileUnits = { "Bytes", "KB", "MB", "GB", "TB" };

	private ProgressDialog myProgressDialog;
	
	public static class FileSize {
		public double fileSize;
		public String unit;

		public FileSize(double size) {
			int i = 0;
			while (size > 1) {
				unit = FileUnits[i];
				fileSize = size;
				size /= 1024;
				i++;

			}
		}

	}

	private DCClientStatus status = DCClientStatus.DISCONNECTED;
	private boolean is_connected = false;
	private DCClient client = null;
	private DCCommand board_message_handler = null;
	private DCCommand user_handler = null;
	private DCCommand search_handler = null;
	private DCPreferences prefs;
	private DCUser myuser;
	public int downloadID = 1;
	NotificationManager mNotifyManager2;
	public LinkedBlockingQueue<DCMessage> search_results;

	public class DownloadObject {
		private NotificationCompat.Builder mBuilder2;
		private String target_nick, local_file, remote_file;
		private DCClient.PassiveDownloadConnection myrc;
		private long milis_began;
		private String file_name;
		private long file_size = 0;
		public int currentID;

		public DCDownloader.DownloadQueueEntity download_status = null;

		public DownloadObject(String target_nick, String local_file,
				String remote_file, DCPPService parent) {
			this.target_nick = target_nick;
			this.local_file = local_file;
			this.remote_file = remote_file;
			String[] file_name_parts = remote_file.split("[\\\\/]");
			file_name = file_name_parts[file_name_parts.length - 1];

			downloadID++;
			this.currentID = downloadID;

			mBuilder2 = new NotificationCompat.Builder(parent);
			this.initialize_download_connection();
		}

		public void initialize_download_connection() {

			if (!remote_file.equalsIgnoreCase("files.xml")) {

				this.mBuilder2.setContentTitle(this.file_name)
						.setContentText("Download Queue")
						.setSmallIcon(R.drawable.ic_launcher);
				mNotifyManager2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mNotifyManager2.notify(this.currentID, mBuilder2.build());

			}

			DCUser my_user = new DCUser();
			DCUser his_user = new DCUser();
			my_user.nick = prefs.getNick();
			his_user.nick = target_nick;
			// initialize_download_tracking();
			myrc = new DCClient.PassiveDownloadConnection(his_user, my_user,
					prefs, local_file, remote_file, client);

		}

		public void start_download() throws InterruptedException {
			set_start_download_time();
			download_status = client.startPassiveDownload(myrc,
					Constants.DOWNLOAD_TIMEOUT_MILLIS);
		}

		public void set_start_download_time() {
			milis_began = System.currentTimeMillis();
		}

		public void set_total_bytes(long size) {
			file_size = size;
		}

		public String getFileName() {
			return file_name;
		}

		public String getTarget_nick() {
			return target_nick;
		}

		public long total_bytes() {
			if (download_status != null
					&& download_status.expectedDownloadSize != 0)
				return download_status.expectedDownloadSize;
			return file_size;
		}

		public long bytes_done() {
			if (download_status != null)
				return download_status.downloadedSize;
			return 0;
		}

		public long millis_elapsed() {
			return System.currentTimeMillis() - milis_began;
		}

		public double avg_speed() {
			return ((double) bytes_done()) / (millis_elapsed() / 1000.0);
		}

		/**
		 * Returns the current status of this download entity.
		 * 
		 * @return
		 */
		public DCConstants.DownloadStatus get_status() {
			if (download_status != null && download_status.status != null) {
				return download_status.status;
			} else {
				return DCConstants.DownloadStatus.UNDEFINED;
			}
		}

		/**
		 * Returns true if this download entity has completed either
		 * successfully or if it failed. Returns false if it has not yet been
		 * initiated.
		 * 
		 * @return
		 */
		public boolean is_finished() {
			if (download_status == null)
				return true;
			return (download_status.status == DCConstants.DownloadStatus.COMPLETED
					|| download_status.status == DCConstants.DownloadStatus.FAILED
					|| download_status.status == DCConstants.DownloadStatus.INTERUPTED || download_status.status == DCConstants.DownloadStatus.SHUTDOWN);
		}
	}

	private Runnable generate_worker(
			final ArrayBlockingQueue<DownloadObject> q,
			final ArrayList<DownloadObject> initiated_list) {
		return new Runnable() {

			@Override
			public void run() {

				try {
					while (true) {
						DownloadObject work = q.take();
						initiated_list.add(work);
						work.start_download();

						work.mBuilder2.setContentTitle(work.file_name)
								.setContentText(work.getFileName())
								.setSmallIcon(R.drawable.ic_launcher);
						mNotifyManager2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

						while (true) {
							if (work.download_status.status == DCConstants.DownloadStatus.COMPLETED) {
								

				                Intent intent = new Intent();
				                intent.setAction(android.content.Intent.ACTION_VIEW);
				                File file = new File(work.local_file);
				              
				                MimeTypeMap mime = MimeTypeMap.getSingleton();
				                String ext=file.getName().substring(file.getName().indexOf(".")+1);
				                String type = mime.getMimeTypeFromExtension(ext);
				             
				                intent.setDataAndType(Uri.fromFile(file),type);
				              
				                PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, 0);
								
								
								work.mBuilder2.setProgress(0, 0, false);
								work.mBuilder2.setContentText(
										"Download Complete")
										.setContentIntent(pIntent)
										.setTicker(
												"Download Complete : "
														+ work.file_name);

								mNotifyManager2.notify(work.currentID,
										work.mBuilder2.build());

								break;

							} else if (work.download_status.status != DCConstants.DownloadStatus.DOWNLOADING) {
								if (work.millis_elapsed() > Constants.DOWNLOAD_TIMEOUT_MILLIS)
									break;
							}
							FileSize fs = new FileSize(work.avg_speed());
							work.mBuilder2.setProgress(
									(int) work.total_bytes(),
									(int) work.bytes_done(), false)
									.setContentText(
											"Download Speed:"
													+ String.format("%.5g%n",
															fs.fileSize) + " "
													+ fs.unit + "/s");
							mNotifyManager2.notify(work.currentID,
									work.mBuilder2.build());

							try {
								Thread.sleep(Constants.DOWNLOAD_UPDATE_INTERVAL_MILLIS);
							} catch (InterruptedException e) {
								Log.d("andcpp", "sleep failure");
							}

							Thread.sleep(Constants.DOWNLOAD_UPDATE_INTERVAL_MILLIS);
						}
						if (work.download_status.status == DCConstants.DownloadStatus.COMPLETED) {
							// Do something here.
							Log.i("andcpp",
									"Download " + work.getFileName()
											+ " completed.");

						} else {
							// Do Something here.
							Log.e("andcpp",
									"Download " + work.getFileName()
											+ " Failed!.");
							
							

							work.mBuilder2.setProgress(0, 0, false);
							work.mBuilder2.setContentText(
									"No slot available on : " + work.target_nick)
									.setTicker(
											"Slot not available on: "
													+ work.target_nick);

							mNotifyManager2.notify(work.currentID,
									work.mBuilder2.build());
							

						}
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		};
	}

	private ArrayBlockingQueue<DownloadObject> normal_queue;
	private ArrayList<DownloadObject> initiated_downloads;
	private Thread normal_worker;

	public void download_file(String nick, String local_file,
			String remote_file, long file_size) {

		final DownloadObject obj = new DownloadObject(nick, local_file,
				remote_file, this);
		obj.set_total_bytes(file_size);
		try {
			normal_queue.put(obj);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Gives all downloads in Queue as well as finished/failed downloads use
	 * member functions of DownloadObject to get details.
	 * 
	 * is_finished() -> Use this to find if it has completed (either
	 * successfully or failed) get_status() -> Use this first to assert its in
	 * Downloading before using others to get progress getFileName()
	 * getTargetNick() total_bytes() bytes_done() millis_ellapsed() avg_speed()
	 * 
	 * @return
	 */
	public List<DownloadObject> get_download_queue() {
		ArrayList<DownloadObject> ret = new ArrayList<DownloadObject>();
		for (DownloadObject o : normal_queue)
			ret.add(o);
		ret.addAll(initiated_downloads);
		return ret;
	}

	/**
	 * Use this to clear all finished/failed downloads
	 */
	public void clear_finished_downloads() {
		ArrayList<DownloadObject> to_remove = new ArrayList<DownloadObject>();
		for (DownloadObject o : initiated_downloads) {
			if (o.is_finished())
				to_remove.add(o);
		}
		initiated_downloads.removeAll(to_remove);
	}

	public DCFileList get_file_list(String nick) {
		File dc_dir = new File(Environment.getExternalStorageDirectory(),
				Constants.dcConfDirectory);
		dc_dir.mkdir();
		File tmp_file_list = new File(dc_dir, "file_list_" + nick + "_"
				+ Long.toString(System.currentTimeMillis()) + ".xml");
		DownloadObject obj = new DownloadObject(nick,
				tmp_file_list.getAbsolutePath(), "files.xml", this);
		try {
			obj.start_download();
			while (true) {
				if (obj.download_status.status == DCConstants.DownloadStatus.COMPLETED)
					break;
				else if (obj.download_status.status != DCConstants.DownloadStatus.DOWNLOADING) {
					if (obj.millis_elapsed() > Constants.DOWNLOAD_TIMEOUT_MILLIS) {
						Log.d("andcpp",
								"download_filelist Timeout reached : " + obj.millis_elapsed());
						break;
					}
				}
				Thread.sleep(Constants.DOWNLOAD_UPDATE_INTERVAL_MILLIS);
			}
			if (obj.download_status.status == DCConstants.DownloadStatus.COMPLETED) {
				DCFileList ret = DCFileList.parseXML(tmp_file_list);
				tmp_file_list.delete();
				return ret;
			} else {
				String s = "null";
				if (obj.download_status.status != null)
					s = obj.download_status.status.name();
				Log.d("andcpp", "download_file_list: Got download status as : " + s);
				return null;
			}
		} catch (InterruptedException e) {
			Log.d("andcpp", "DCPPService: Got an interrupted exception");
			return null;
		} catch (Exception e) {
			Log.d("andcpp", "DCPPService: Got an unknow exception in get_file_list");
			return null;
		}

	}

	public void setBoard_message_handler(DCCommand handler) {
		board_message_handler = handler;
	}

	public void setUser_handler(DCCommand handler) {
		user_handler = handler;
	}

	public void setSearch_handler(DCCommand handler) {
		search_handler = handler;
	}

	private class MyUserHandler implements DCCommand {
		@Override
		public void onCommand(DCMessage dcMessage) {
			if (user_handler != null)
				user_handler.onCommand(dcMessage);
		}
	}

	private class MySearchHandler implements DCCommand {
		@Override
		public void onCommand(DCMessage dcMessage) {
			if (search_handler != null)
				search_handler.onCommand(dcMessage);
			search_results.add(dcMessage);
		}
	}

	private class MyBoardMessageHandler implements DCCommand {
		@Override
		public void onCommand(DCMessage dcMessage) {
			if (board_message_handler != null)
				board_message_handler.onCommand(dcMessage);
		}
	}

	public DCClientStatus get_status() {
		return status;
	}

	public List<DCUser> get_nick_list() {
		return client.get_nick_list();
	}

	public List<String> get_board_messages() {
		ArrayList<String> ret = new ArrayList<String>();
		for(DCMessage msg: client.getBoardMessages()) {
			ret.add(msg.msg_s);
		}
		return ret;
	}

	public DCPPService() {
		super("DCPP Service");
		search_results = new LinkedBlockingQueue<DCMessage>();
	}

	public class LocalBinder extends Binder {
		DCPPService getService() {
			return DCPPService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	public IBinder onBind(Intent intent) {
		Log.d("andcpp", "Binding to service");
		return mBinder;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (!is_connected) {
			Bundle data = intent.getExtras();
			if (data.containsKey("nick") && data.containsKey("ip")) {
				String nick = data.getString("nick");
				String ip = data.getString("ip");
				prefs = new DCPreferences(nick, 3000L * 1024 * 1024, ip);
				myuser = new DCUser();
				myuser.nick = nick;
				client = new DCClient();
				try {
					client.connect(ip, 411, prefs);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					exceptionCaught();
					return;
				} catch (InterruptedException e) {
					e.printStackTrace();
					exceptionCaught();
					return;
				} catch (IOException e) {
					e.printStackTrace();
					exceptionCaught();
					return;
				}		
				
				mySendBroadcast(false, true);
				
				client.bootstrap(myuser);
				client.setCustomUserChangeHandler(new MyUserHandler());
				client.setCustomBoardMessageHandler(new MyBoardMessageHandler());
				client.setCustomSearchHandler(new MySearchHandler());
				client.InitiateDefaultRouting();
				status = DCClientStatus.CONNECTED;
				is_connected = true;
				Intent notification_intent = new Intent(this,
						MainActivity.class);
				PendingIntent pendingIntent = PendingIntent.getActivity(this,
						0, notification_intent, 0);
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
						this);
				mBuilder.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle("AnDC++ Client Running")
						.setContentText("AnDC++ is currently running")
						.setTicker("Connecting to hub " + ip)
						.setContentIntent(pendingIntent);
				initiated_downloads = new ArrayList<DownloadObject>();
				normal_queue = new ArrayBlockingQueue<DownloadObject>(
						Constants.MAX_DOWNLOAD_Q);
				normal_worker = new Thread(generate_worker(normal_queue,
						initiated_downloads));
				normal_worker.start();
				startForeground(1, mBuilder.build());
				try {
					synchronized (this) {
						this.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("andcpp",
						"dcpp_service Received intent without nick or ip parameters");
				status = DCClientStatus.INVALIDIP;
			}
		} else {
			Log.e("andcpp",
					"dcpp_service Attempted to connect while already connected");
		}
	}

	private void exceptionCaught() {
		Log.d("andcpp", "exceptionCaught!");
		Toast.makeText(getApplicationContext(), "Unable to connect", Toast.LENGTH_LONG)
			.show();
		mySendBroadcast(true, true);
	}
	
	private void mySendBroadcast(Boolean stopServiceFlag, Boolean stopProgressDialogFlag){
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("ACTION_DO_SOMETHING");
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);		
		broadcastIntent.putExtra("stopServiceFlag", stopServiceFlag);
		broadcastIntent.putExtra("stopProgressDialogFlag", stopProgressDialogFlag);
		sendBroadcast(broadcastIntent);
	}

	public void shutdown() {
		if (is_connected) {
			is_connected = false;
			status = DCClientStatus.DISCONNECTED;
			stopForeground(true);
			synchronized (this) {
				this.notify();
			}
			// TODO Shutdown the DClient.
			normal_worker.interrupt();
		}
	}

	public void make_search(String searchtext) {
		search_results.clear();
		search_results = new LinkedBlockingQueue<DCMessage>(); //reset search results
		Log.d("andcpp", "dcpp_service Making search request " + searchtext);
		client.searchForFile(searchtext, myuser, 1); // 1 is for files
	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		// Let's shutdown connection(if any) if app get's unexpectedly killed.
		shutdown();
		Log.d("andcpp", "destroyed!");
	}

	
	
	
	
}
