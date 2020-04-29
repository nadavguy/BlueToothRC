package com.example.fragmentviewmodeltest.ui.main;

import androidx.lifecycle.ViewModelProviders;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fragmentviewmodeltest.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@Getter
@Setter
public class MainFragment extends Fragment
{

    private MainViewModel mViewModel;
    private Button mPairedDevicesButton;
    private Button mSendButton;
    private ListView mDeviceListView;
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mFasterButton;
    private Button mSlowerButton;
    private Button mLeftButton;
    private Button mRightButton;
    private Button mStopButton;
    private Button mCenterButton;
    private TextView mSpeedValue;
    private TextView mAngleValue;
    private TextView mMessage;


    private int SpeedValue = 0;
    private int AngleValue = 90;
    private int MessageCounter = 0;

    private boolean SocketOpened = false;

    private Set<BluetoothDevice> mPairedDevices;
    private BluetoothAdapter mBTAdapter;
    private ArrayAdapter<String> mBTArrayAdapter;
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path
    private Handler mHandler;
    private Handler MonitorControlshandler;
    private Handler MonitorBThandler;
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    // Added Coment


    public static MainFragment newInstance()
    {
        return new MainFragment();
    }

    private static MainFragment outInstance = new MainFragment();
    public static MainFragment getInstance() { return  outInstance;}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.main_fragment, container, false);

//        Declare UI components
        mPairedDevicesButton = (Button) root.findViewById(R.id.PairedDevicesButton);
        mDeviceListView = root.findViewById(R.id.DevicesListView);
        mBluetoothStatus = root.findViewById(R.id.BTTextView);
        mSendButton = root.findViewById(R.id.SendButton);

        mFasterButton = root.findViewById(R.id.IncreaseSpeedButton);
        mSlowerButton = root.findViewById(R.id.DecreaseSpeedButton);
        mLeftButton = root.findViewById(R.id.LeftButton);
        mRightButton = root.findViewById(R.id.RightButton);
        mStopButton = root.findViewById(R.id.StopButton);
        mCenterButton = root.findViewById(R.id.CenterButton);

        mSpeedValue = root.findViewById(R.id.SpeedValueTextView);
        mAngleValue = root.findViewById(R.id.AngleValueTextView);
        MainFragment.getInstance().setMAngleValue((TextView) root.findViewById(R.id.AngleValueTextView));
        MainFragment.getInstance().setMSpeedValue((TextView) root.findViewById(R.id.SpeedValueTextView));
        MainFragment.getInstance().setMMessage((TextView) root.findViewById(R.id.messageTextView));
        MainFragment.getInstance().setSocketOpened(false);

        MainFragment.getInstance().setMDeviceListView((ListView) root.findViewById(R.id.DevicesListView));

//dsfsfsfdsfsf



//        Declare Adaptors
        mBTArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
//        mDeviceListView.setOnItemClickListener(mDeviceClickListener); // What to do when Item in list is selected
        MainFragment.getInstance().getMDeviceListView().setOnItemClickListener(mDeviceClickListener);

        //Assign Adaptor to UI display Component
//        mDeviceListView.setAdapter(mBTArrayAdapter);
//        mDeviceListView.setVisibility(View.VISIBLE);
        MainFragment.getInstance().getMDeviceListView().setAdapter(mBTArrayAdapter);
        MainFragment.getInstance().getMDeviceListView().setVisibility(View.VISIBLE);


        mPairedDevicesButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listPairedDevices(v);
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                mConnectedThread.write("aaa\r\n");
//                MainFragment.getInstance().getMConnectedThread().write("aaa\r\n");
            }
        });

        mFasterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //SpeedValue = Math.min(SpeedValue+5,100);
                MainFragment.getInstance().setSpeedValue(Math.min(MainFragment.getInstance().getSpeedValue()+5,100));

            }
        });

        mSlowerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                SpeedValue = Math.max(SpeedValue-5,0);
                MainFragment.getInstance().setSpeedValue(Math.max(MainFragment.getInstance().getSpeedValue()-5,100));
            }
        });

        mLeftButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                AngleValue = Math.max(AngleValue-5,35);
                MainFragment.getInstance().setAngleValue(Math.min(MainFragment.getInstance().getAngleValue()+5,145));
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                AngleValue = Math.min(AngleValue + 5 , 145);

                MainFragment.getInstance().setAngleValue(Math.max(MainFragment.getInstance().getAngleValue()-5,35));
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainFragment.getInstance().setSpeedValue(0);
            }
        });

        mCenterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                AngleValue = 90;
                MainFragment.getInstance().setAngleValue(90);
            }
        });

        mHandler = new Handler()
        {
            public void handleMessage(android.os.Message msg)
            {
                if (msg.what == MESSAGE_READ)
                {
                    String readMessage = null;
                    try
                    {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                    }
//                    mReadBuffer.setText(readMessage);
                }

                if (msg.what == CONNECTING_STATUS)
                {
                    if (msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + (String) (msg.obj));
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }
        };
        //TODO: Do not remove, Part of Thread example
//        MonitorControls MonitorControlThread = new MonitorControls();
////        MonitorControlThread.start();

        MonitorBThandler = new Handler();
        MonitorBThandler.post(MonitorBTChannel);

        MonitorControlshandler = new Handler();
        MonitorControlshandler.post(runnableCode);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    private void listPairedDevices(View view)
    {
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled())
        {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
            {
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            Toast.makeText(getContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3)
        {

            if (!mBTAdapter.isEnabled())
            {
                Toast.makeText(getContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0, info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run()
                {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try
                    {
                        mBTSocket = createBluetoothSocket(device);
                        MainFragment.getInstance().setMBTSocket(mBTSocket);
                    }
                    catch (IOException e)
                    {
                        fail = true;
                        Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try
                    {
                        mBTSocket.connect();
                    }
                    catch (IOException e)
                    {
                        try
                        {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        }
                        catch (IOException e2)
                        {
                            //insert code to deal with this
                            Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (fail == false)
                    {
//                        mConnectedThread = new ConnectedThread(mBTSocket);
//                        MainFragment.getInstance().setMConnectedThread(mConnectedThread);
//                        mConnectedThread.start();
//                        MainFragment.getInstance().getMConnectedThread().start();
                        MainFragment.getInstance().setSocketOpened(true);
                        MainFragment.getInstance().getMDeviceListView().setVisibility(View.INVISIBLE);
                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true)
            {
                try
                {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if (bytes != 0)
                    {
                        SystemClock.sleep(100);
                        mmInStream.read(buffer);
                    }
                    // Send the obtained bytes to the UI activity

                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                }
                catch (IOException e)
                {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input)
        {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try
            {
                mmOutStream.write(bytes);
            }
            catch (IOException e)
            {
                int a = 1;
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    //Other option to Thread is Handler
    //https://guides.codepath.com/android/Repeating-Periodic-Tasks
    //https://stackoverflow.com/questions/11639251/android-run-a-thread-repeatingly-within-a-timer


    class MonitorControls extends Thread
    {
        String MessageToSend = "";
        String SticksMessageHeader = "AB56FE21,";
        MonitorControls()
        {


        }

        public void run()
        {
            while (true)
            {
                MainFragment.getInstance().getMAngleValue().setText(Integer.toString(MainFragment.getInstance().getAngleValue()));
                String MessageBody = "#" + Integer.toString(MainFragment.getInstance().getSpeedValue()) + "," + Integer.toString(MainFragment.getInstance().getAngleValue()) + "~";
                MainFragment.getInstance().setMessageCounter(MainFragment.getInstance().getMessageCounter()+1);
                MessageToSend = SticksMessageHeader + MessageBody.length() + "," + MainFragment.getInstance().getMessageCounter() + "," + MessageBody;
//            MainFragment.getInstance().getMConnectedThread().write(MessageToSend + "\r\n");

            }
        }
    }

    private Runnable runnableCode = new Runnable() {
        String MessageToSend = "";
        String SticksMessageHeader = "AB56FE21,";
        @Override
        public void run() {
            // Do something here on the main thread
            MainFragment.getInstance().getMAngleValue().setText(Integer.toString(MainFragment.getInstance().getAngleValue()));
            MainFragment.getInstance().getMSpeedValue().setText(Integer.toString(MainFragment.getInstance().getSpeedValue()));
//            MessageToSend = "#" + SticksMessageHeader + Integer.toString(MainFragment.getInstance().getSpeedValue()) + "," + Integer.toString(MainFragment.getInstance().getAngleValue()) + "~\r\n";
            String MessageBody = "#" + Integer.toString(MainFragment.getInstance().getSpeedValue()) + "," + Integer.toString(MainFragment.getInstance().getAngleValue()) + "!~";
            MainFragment.getInstance().setMessageCounter(MainFragment.getInstance().getMessageCounter()+1);
            MessageToSend = SticksMessageHeader + MessageBody.length() + "," + MainFragment.getInstance().getMessageCounter() + "," + MessageBody;
            if ( !(MainFragment.getInstance().isSocketOpened()) )
            {

            }
            else
            {
                byte[] bytes = MessageToSend.getBytes();
                try
                {
                    OutputStream A;
                    A = MainFragment.getInstance().getMBTSocket().getOutputStream();
                    A.write(bytes,0,bytes.length);
                    A.flush();
                    //Thread.sleep(500);
                }
                catch (IOException e)
                {

                }

            }

            // Repeat this the same runnable code block again another 0.1 seconds
            // 'this' is referencing the Runnable object
            MonitorControlshandler.postDelayed(this, 100);
        }
    };

    private Runnable MonitorBTChannel = new Runnable()
    {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        @SneakyThrows
        @Override
        public void run()
        {
            mmSocket = MainFragment.getInstance().getMBTSocket();

            if ( !(mmSocket == null) )
            {
                // Get the input and output streams, using temp objects because
                // member streams are final
                try
                {
                    mmInStream = mmSocket.getInputStream();
                    mmOutStream = mmSocket.getOutputStream();
                }
                catch (IOException e)
                {
                }

                byte[] buffer = new byte[1024];  // buffer store for the stream
                int bytes = 0; // bytes returned from read()

                try
                {
                    bytes = mmInStream.available();
                }
                catch (IOException e)
                {

                }
//                MainFragment.getInstance().setMessageCounter(MainFragment.getInstance().getMessageCounter()+1);
//                MainFragment.getInstance().getMMessage().setText(Integer.toString(MainFragment.getInstance().getMessageCounter()));
                if (bytes != 0)
                {
                    //SystemClock.sleep(100);
                    mmInStream.read(buffer);
                    MainFragment.getInstance().getMMessage().setText(new String(buffer));
                }
            }
            else
            {
                int a = 1;
            }

            MonitorBThandler.postDelayed(this, 100);
        }

        private void SendMessage(String MessageToSend)
        {
            byte[] bytes = MessageToSend.getBytes();           //converts entered String into bytes
            try
            {
                mmOutStream.write(bytes);
            }
            catch (IOException e)
            {
                int a = 1;
            }
        }
    };
//Thread Creation example
    //https://developer.android.com/reference/java/lang/Thread
//    class PrimeThread extends Thread {
//        long minPrime;
//        PrimeThread(long minPrime) {
//            this.minPrime = minPrime;
//        }
//
//        public void run() {
//            // compute primes larger than minPrime
//              . . .
//        }
//    }

//    PrimeThread p = new PrimeThread(143);
//     p.start();
}
