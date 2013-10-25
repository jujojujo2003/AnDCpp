package com.phinmadvader.andcpp;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.phinvader.libjdcpp.DCClient;
import com.phinvader.libjdcpp.DCCommand;
import com.phinvader.libjdcpp.DCConstants;
import com.phinvader.libjdcpp.DCDownloader;
import com.phinvader.libjdcpp.DCFileList;
import com.phinvader.libjdcpp.DCMessage;
import com.phinvader.libjdcpp.DCPreferences;
import com.phinvader.libjdcpp.DCUser;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by phinfinity on 11/08/13.
 */
public class DCPPService extends IntentService {
    public static enum DCClientStatus {
        DISCONNECTED, INVALIDIP, CONNECTED
    }

    private DCClientStatus status = DCClientStatus.DISCONNECTED;
    private boolean is_connected = false;
    private DCClient client = null;
    private DCCommand board_message_handler = null;
    private DCCommand user_handler = null;
    private DCCommand search_handler = null;
    private DCPreferences prefs;

    public class DownloadObject {
        private String target_nick, local_file, remote_file;
        private DCClient.PassiveDownloadConnection myrc;
        private long milis_began;
        private String file_name;
        private long file_size = 0;
        public DCDownloader.DownloadQueueEntity download_status = null;

        public DownloadObject(String target_nick, String local_file, String remote_file) {
            this.target_nick = target_nick;
            this.local_file = local_file;
            this.remote_file = remote_file;
            String[] file_name_parts = remote_file.split("/");
            file_name = file_name_parts[file_name_parts.length - 1];
            this.initialize_download_connection();
        }
        public void initialize_download_connection() {
            DCUser my_user = new DCUser();
            DCUser his_user = new DCUser();
            my_user.nick = prefs.getNick();
            his_user.nick = target_nick;
            myrc = new DCClient.PassiveDownloadConnection(his_user, my_user, prefs, local_file, remote_file, client);
        }
        public void start_download() throws InterruptedException {
            set_start_download_time();
            download_status = client.startPassiveDownload(myrc, Constants.DOWNLOAD_TIMEOUT_MILLIS);
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
            if (download_status != null && download_status.expectedDownloadSize != 0)
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
            return ((double)bytes_done())/(millis_elapsed()/1000.0);
        }

        /**
         * Returns the current status of this download entity.
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
         * Returns true if this download entity has completed either successfully or if it failed.
         * Returns false if it has not yet been initiated.
         * @return
         */
        public boolean is_finished() {
            return (download_status.status == DCConstants.DownloadStatus.COMPLETED ||
                    download_status.status == DCConstants.DownloadStatus.FAILED ||
                    download_status.status == DCConstants.DownloadStatus.INTERUPTED ||
                    download_status.status == DCConstants.DownloadStatus.SHUTDOWN);
        }
    }

    private Runnable generate_worker(final ArrayBlockingQueue<DownloadObject> q, final ArrayList<DownloadObject> initiated_list) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        DownloadObject work = q.take();
                        initiated_list.add(work);
                        work.start_download();
                        while (true) {
                            if (work.download_status.status == DCConstants.DownloadStatus.COMPLETED)
                                break;
                            else if (work.download_status.status != DCConstants.DownloadStatus.DOWNLOADING) {
                                if (work.millis_elapsed() > Constants.DOWNLOAD_TIMEOUT_MILLIS)
                                    break;
                            }
                            Thread.sleep(Constants.DOWNLOAD_UPDATE_INTERVAL_MILLIS);
                        }
                        if (work.download_status.status == DCConstants.DownloadStatus.COMPLETED) {
                            // Do something here.
                            Log.i("download_service", "Download " + work.getFileName() + " completed.");
                            //Toast.makeText(DCPPService.this, "Download " + work.getFileName() + " completed.", Toast.LENGTH_SHORT);
                        } else  {
                            // Do Something here.
                            Log.e("download_service", "Download " + work.getFileName() + " Failed!.");
                            //Toast.makeText(DCPPService.this, "Download " + work.getFileName() + " Failed!.", Toast.LENGTH_SHORT);
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

    public void download_file(String nick, String local_file, String remote_file, long file_size)  {
        DownloadObject obj = new DownloadObject(nick,local_file,remote_file);
        obj.set_total_bytes(file_size);
        try {
            normal_queue.put(obj);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gives all downloads in Queue as well as finished/failed downloads
     * use member functions of DownloadObject to get details.
     *
     * is_finished() -> Use this to find if it has completed (either successfully or failed)
     * get_status() -> Use this first to assert its in Downloading before using others to get progress
     * getFileName()
     * getTargetNick()
     * total_bytes()
     * bytes_done()
     * millis_ellapsed()
     * avg_speed()
     * @return
     */
    public List<DownloadObject> get_download_queue() {
        ArrayList<DownloadObject> ret = new ArrayList<DownloadObject>();
        for(DownloadObject o : normal_queue)
            ret.add(o);
        ret.addAll(initiated_downloads);
        return ret;
    }

    /**
     * Use this to clear all finished/failed downloads
     */
    public void clear_finished_downloads() {
        ArrayList<DownloadObject> to_remove = new ArrayList<DownloadObject>();
        for(DownloadObject o : initiated_downloads) {
            if (o.is_finished())
                to_remove.add(o);
        }
        initiated_downloads.removeAll(to_remove);
    }

    public DCFileList get_file_list(String nick) {
        File dc_dir = new File(Environment.getExternalStorageDirectory(), Constants.dcConfDirectory);
        dc_dir.mkdir();
        File tmp_file_list = new File(dc_dir,
                "file_list_" + nick + "_" + Long.toString(System.currentTimeMillis())  + ".xml");
        DownloadObject obj = new DownloadObject(nick, tmp_file_list.getAbsolutePath(), "files.xml");
        try {
            obj.start_download();
            while (true) {
                if (obj.download_status.status == DCConstants.DownloadStatus.COMPLETED)
                    break;
                else if (obj.download_status.status != DCConstants.DownloadStatus.DOWNLOADING) {
                    if (obj.millis_elapsed() > Constants.DOWNLOAD_TIMEOUT_MILLIS) {
                        Log.d("download_file_list", "Timeout reached : " + obj.millis_elapsed());
                        break;
                    }
                }
                Thread.sleep(Constants.DOWNLOAD_UPDATE_INTERVAL_MILLIS);
            }
            if (obj.download_status.status == DCConstants.DownloadStatus.COMPLETED) {
                DCFileList ret = DCFileList.parseXML(tmp_file_list);
                tmp_file_list.delete();
                return ret;
            } else  {
                String s = "null";
                if (obj.download_status.status != null)
                    s = obj.download_status.status.name();
                Log.d("download_file_list", "Got download status as : " + s );
                return null;
            }
        } catch (InterruptedException e) {
            Log.d("DCPPService", "Got an interrupted exception");
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
            if(user_handler != null)
                user_handler.onCommand(dcMessage);
        }
    }
    private class MySearchHandler implements DCCommand {
        @Override
        public void onCommand(DCMessage dcMessage) {
            if(search_handler != null)
                search_handler.onCommand(dcMessage);
        }
    }
    private class MyBoardMessageHandler implements  DCCommand {
        @Override
        public void onCommand(DCMessage dcMessage) {
            if(board_message_handler != null)
                board_message_handler.onCommand(dcMessage);
        }
    }

    public DCClientStatus get_status() {
        return status;
    }

    public List<DCUser> get_nick_list() {
        return client.get_nick_list();
    }

    public DCPPService() {
        super("DCPP Service");
    }


    public class LocalBinder extends Binder {
        DCPPService getService() {
            return DCPPService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    public IBinder onBind(Intent intent) {
        Log.d("dcpp_service", "Binding to service");
        return mBinder;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (!is_connected) {
            Bundle data = intent.getExtras();
            if (data.containsKey("nick") && data.containsKey("ip")) {
                String nick = data.getString("nick");
                String ip = data.getString("ip");
                prefs = new DCPreferences(nick, 0, ip);
                
                DCUser myuser = new DCUser();
                myuser.share_size = 3000000000L;
                
                myuser.nick = nick;
                client = new DCClient();
                try {
                    client.connect(ip, 411, prefs);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                client.bootstrap(myuser);
                client.setCustomUserChangeHandler(new MyUserHandler());
                client.setCustomBoardMessageHandler(new MyBoardMessageHandler());
                client.setCustomSearchHandler(new MySearchHandler());
                client.InitiateDefaultRouting();
                status = DCClientStatus.CONNECTED;
                is_connected = true;
                Intent notification_intent = new Intent(this, ConnectActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notification_intent, 0);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("AnDC++ Client Running")
                        .setContentText("AnDC++ is currently running")
                        .setTicker("Connecting to hub " + ip)
                        .setContentIntent(pendingIntent);
                initiated_downloads = new ArrayList<DownloadObject>();
                normal_queue = new ArrayBlockingQueue<DownloadObject>(Constants.MAX_DOWNLOAD_Q);
		        normal_worker = new Thread(generate_worker(normal_queue, initiated_downloads));
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
                Log.e("dcpp_service", "Received intent without nick or ip parameters");
                status = DCClientStatus.INVALIDIP;
            }
        } else {
            Log.e("dcpp_service", "Attempted to connect while already connected");
        }
    }

    public void shutdown() {
        if (is_connected) {
            is_connected = false;
            status=DCClientStatus.DISCONNECTED;
            stopForeground(true);
            synchronized (this) {
                this.notify();
            }
            // TODO Shutdown the DClient.
            normal_worker.interrupt();
        }
    }
}
