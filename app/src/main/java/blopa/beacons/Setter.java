package blopa.beacons;

/**
 * Created by Blopa on 26-12-2016.
 */


//
//public class Setter {
//
//    private void setMajorID(final int majorid,final Beacon beacon) {
//
//        mMajorsConnection = new BeaconConnection(this, beacon, new BeaconConnection.ConnectionCallback() {
//            @Override
//            public void onAuthenticated(BeaconConnection.BeaconCharacteristics chars) {
//                Log.d(TAG, "Authenticated to beacon: " + chars);
//                mMajorsConnection.writeMajor(majorid, new BeaconConnection.WriteCallback() {
//                    @Override
//                    public void onSuccess() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mAdapter.update(beacon);
//                            }
//                        });
//                        Log.d(TAG, "Successfully writted the major id!");
//                        mMajorsConnection.close();
//                    }
//
//                    @Override
//                    public void onError() {
//                        Log.d(TAG, "Error while writting the major id!");
//                    }
//                });
//            }
//
//            @Override
//            public void onAuthenticationError() {
//                Log.d(TAG, "Authentication Error");
//            }
//
//            @Override
//            public void onDisconnected() {
//                Log.d(TAG, "Disconnected");
//            }
//        });
//        mMajorsConnection.authenticate();
//    }
//}
