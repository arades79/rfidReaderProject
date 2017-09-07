//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.nzar.toyotarfid.SettingsActivity.settings;

/**
 * Created by cravers on 6/29/2017.
 */

/*
The Database Connector class is where all interactions with the designated database for this project will happen
This class grabs the data for the equipment being used, the person who is trying to badge in, the ppe requirements
and is also responsible for sending the appropriate data back to the database for keeping logs
*/
class DatabaseConnector extends AppCompatActivity {

    public static ArrayList<PPE> PPEList = new ArrayList<>();
    public static ArrayList<LabTech> LabTechList = new ArrayList<>();
    public static int currentSessionID;
    public static String baseServerUrl = "10.2.5.50";
    public static String currentBadgeID = "";

    static class LabTech{
        int LabTechID;
        String firstName;
        String lastName;
        String email;
        String phoneNumber;
        Bitmap Image;
      }

    static class PPE {
        int PPEID;
        String name;
        Bitmap Image;
        boolean Required;
        boolean Restricted;
    }

      
    private static JsonReader TILTAPITask(HttpURLConnection connection, String method) throws Exception {
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "basic VElMVFdlYkFQSToxM1RJTFRXZWJBUEkxMw==");

        if (connection.getResponseCode() == 201 || connection.getResponseCode() == 200) {
            InputStream RawResponse = connection.getInputStream();
            InputStreamReader Response = new InputStreamReader(RawResponse, "UTF-8");
            return new JsonReader(Response);


        } else if(connection.getResponseCode() == 400) {
            connection.disconnect();
            return null;
        } else {
            connection.disconnect();
            throw new Exception("bad http response ");
        }
    }

    private static Bitmap ImageParser(String jsonImage) throws UnsupportedEncodingException {
        byte encodedImage[] = jsonImage.getBytes();
        return BitmapFactory.decodeByteArray(encodedImage, 15, encodedImage.length);
    }

//give the badge number as a string, provide progress messages as Strings, and return a Boolean if the user is allowed
    static class TILTPostUserTask extends AsyncTask<String, String, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String machineIP = "123";
            String badgeID = params[0];
            String isLoggingOut, sessionID;
            if (params.length == 2) {
                sessionID = params[1];
                isLoggingOut = "true";
            } else if (params.length == 1){
                currentSessionID = new Random().nextInt();
                sessionID = String.valueOf(currentSessionID);
                currentBadgeID = badgeID;
                isLoggingOut = "false";
            } else {
                return null;
            }

            try {
                URL url = new URL("http://" +
                        baseServerUrl +
                        "/tiltwebapi/api/users?" +
                        "sessionID=" + sessionID +
                        "&machineIP=" + machineIP +
                        "&badgeID=" + badgeID +
                        "&isLoggingOut=" + isLoggingOut);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();


                JsonReader Response = TILTAPITask(connection,"POST");


                assert Response != null;
                Response.beginArray();

                PPEList.clear();
                while(Response.hasNext()) {
                    PPE ppe = new PPE();
                    Response.beginObject();
                    while (Response.hasNext()) {
                        //parse response for PPE info
                        //if the response is not empty, set UserAuthorized to true
                        String key = Response.nextName();
                        switch (key) {
                            case "PPE":
                                ppe.name = Response.nextString();
                                break;
                            case "Image":
                                ppe.Image = ImageParser(Response.nextString());
                                break;
                            case "Required":
                                ppe.Required = Response.nextBoolean();
                                break;
                            case "Restricted":
                                ppe.Restricted = Response.nextBoolean();
                                break;
                            default:
                                Response.skipValue();
                                break;
                        }
                    }
                    Response.endObject();
                }
                Response.endArray();

                Response.close();
                connection.disconnect();
                return true;




            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;





        }
    }

    static class TILTPostTechTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String sessionID = String.valueOf(currentSessionID) ;
            String machineIP = "123";
            String content = "I sent the tech an Email!!";//content of the email message
            try {
                URL url = new URL("http://" +
                        baseServerUrl +
                        "/tiltwebapi/api/Technicians" +
                        "?sessionID=" + sessionID +
                        "&machineIP="+ machineIP +
                        "&content="+ content);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();


                TILTAPITask(connection, "POST");

                connection.disconnect();


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    static class TILTGetTechTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://" +
                        baseServerUrl +
                        "/tiltwebapi/api/Technicians");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                JsonReader ResponseReader = TILTAPITask(connection, "GET");
                assert ResponseReader != null;
                LabTechList.clear();
                ResponseReader.beginArray();
                while (ResponseReader.hasNext()) {
                    LabTech temp = new LabTech();

                    ResponseReader.beginObject();
                    while (ResponseReader.hasNext()) {
                        String key = ResponseReader.nextName();
                        switch (key) {
                            case ("LabTechID"):
                                temp.LabTechID = ResponseReader.nextInt();
                                break;
                            case ("FirstName"):
                                temp.firstName = ResponseReader.nextString();
                                break;
                            case ("LastName"):
                                temp.lastName = ResponseReader.nextString();
                                break;
                            case ("Email"):
                                temp.email = ResponseReader.nextString();
                                break;
                            case ("PhoneNumber"):
                                temp.email = ResponseReader.nextString();
                                break;
                            case "Image":
                                temp.Image = ImageParser(ResponseReader.nextString());
                            default:
                                ResponseReader.skipValue();
                                break;
                        }
                    }
                    LabTechList.add(temp);
                    ResponseReader.endObject();

                }
                ResponseReader.endArray();
                ResponseReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    //take the app context, doesn't provide updates, and return a boolean
    public static class SetNetworkConfigTask extends AsyncTask<Context,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Context... params) {

            WifiConfiguration wifiConf = null;
            WifiManager wifiManager = (WifiManager) params[0].getSystemService(Context.WIFI_SERVICE);
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.networkId == connectionInfo.getNetworkId()) {
                    wifiConf = conf;
                    break;
                }
            }
            try {
                NetworkConfigurator.setIpAssignment("STATIC", wifiConf); //or "DHCP" for dynamic setting
                NetworkConfigurator.setIpAddress(InetAddress.getByName(settings.getString("static_ip", "192.168.0.235")), 24, wifiConf);
                NetworkConfigurator.setDNS(InetAddress.getByName("8.8.8.8"), wifiConf);
                wifiManager.updateNetwork(wifiConf); //apply the setting
                wifiManager.saveConfiguration(); //Save it
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }


    @SuppressWarnings("unchecked")
    private static class NetworkConfigurator {

        static void setIpAssignment(String assign, WifiConfiguration wifiConf)
                throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
            setEnumField(wifiConf, assign, "ipAssignment");
        }

        static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf)
                throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
                NoSuchMethodException, ClassNotFoundException, InstantiationException, InvocationTargetException {
            Object linkProperties = getField(wifiConf, "linkProperties");
            if (linkProperties == null) return;
            Class laClass = Class.forName("android.net.LinkAddress");
            Constructor laConstructor = laClass.getConstructor(InetAddress.class, int.class);
            Object linkAddress = laConstructor.newInstance(addr, prefixLength);

            ArrayList mLinkAddresses = (ArrayList) getDeclaredField(linkProperties, "mLinkAddresses");
            mLinkAddresses.clear();
            mLinkAddresses.add(linkAddress);
        }

        public static void setGateway(InetAddress gateway, WifiConfiguration wifiConf)
                throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
                ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
            Object linkProperties = getField(wifiConf, "linkProperties");
            if (linkProperties == null) return;
            Class routeInfoClass = Class.forName("android.net.RouteInfo");
            Constructor routeInfoConstructor = routeInfoClass.getConstructor(InetAddress.class);
            Object routeInfo = routeInfoConstructor.newInstance(gateway);

            ArrayList mRoutes = (ArrayList) getDeclaredField(linkProperties, "mRoutes");
            mRoutes.clear();
            mRoutes.add(routeInfo);
        }

        static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
                throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
            Object linkProperties = getField(wifiConf, "linkProperties");
            if (linkProperties == null) return;

            ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mDnses");
            mDnses.clear(); //or add a new dns address , here I just want to replace DNS1
            mDnses.add(dns);
        }

        static Object getField(Object obj, String name)
                throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getField(name);
            return f.get(obj);
        }

        static Object getDeclaredField(Object obj, String name)
                throws SecurityException, NoSuchFieldException,
                IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        }

        static void setEnumField(Object obj, String value, String name)
                throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getField(name);
            f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
        }

    }


}