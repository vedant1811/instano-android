//package com.instano.retailer.instano;
//
///**
// * Created by Akash on 13-06-2014.
// */
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.database.SQLException;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteException;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.graphics.PointF;
//import android.util.Log;
//
//public class RetailerDatabaseHelper extends SQLiteOpenHelper{
//
//    private static final int DATABASE_VERSION = 1;
//    //The Android's default system path of your application database.
//    public static String DB_PATH; //= "/data/data/com.ai.instano/databases/";
//    public static final String DB_NAME = "data";
//
//    // Tables and Fields
//    public static final String TABLE_RETAILER = "retailer_info";
//        public static final String FIELD_RETAILER_ID = "_id";
//        public static final String FIELD_RETAILER_NAME = "retailer_name";
//        public static final String FIELD_RETAILER_LAT = "latitude";
//        public static final String FIELD_RETAILER_LNG = "longitude";
//        public static final String FIELD_RETAILER_PHONE_NUMBER = "phone_number";
//        public static final String FIELD_RETAILER_ADDRESS = "address";
////    public static final String TABLE_PRODUCTS = "ProductInfo";
////    public static final String FIELD_PRODUCT_ID = "ID";
////    public static final String FIELD_PRODUCT_NAME = "ProductName";
////    public static final String FIELD_PRODUCT_CATEGORY = "ProductCategory";
////    public static final String FIELD_PRODUCT_TYPE = "ProductType";
////    public static final String FIELD_PRODUCT_CODE = "ProductCode";
////    public static final String FIELD_PRODUCT_BRAND = "Brand";
////    public static final String TABLE_PRODUCT_AVAIL = "ProductAvailability";
////    public static final String FIELD_PA_PRODUCT_ID = "ProductID";
////    public static final String FIELD_PA_RETAIL_ID = "RetailerID";
////    public static final String FIELD_PA_PRICE = "Price";
////    public static final String FIELD_PA_QTY_AVAIL = "QtyAvail";
//
//
//    private SQLiteDatabase mDatabase;
//    private final Context mContext;
//    //private Map<String, String> tables = new HashMap<String, String>();
//
//    /**
//     * Constructor
//     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
//     * @param context
//     */
//    public RetailerDatabaseHelper(Context context) {
//        super(context, DB_NAME, null, DATABASE_VERSION);
//        this.mContext = context;
//        DB_PATH="/data/data/" + context.getPackageName() + "/" + "databases/";
//        try {
//            createDataBase();
//        } catch (IOException e) {
//            throw new Error("Error creating database");
//        }
//        mDatabase = this.getWritableDatabase(); // TODO: move this to an async task
//
//    }
//
//    public void createDataBase() throws IOException{
//
//        if(checkDataBase()){
//            Log.d("database", "database exists!!");
//            //do nothing - database already exist
//        }else{
//
//            //By calling this method and empty database will be created into the default system path
//            //of your application so we are gonna be able to overwrite that database with our database.
//            Log.d("database", "database doesn't exist. Creating one.");
//            this.getReadableDatabase();
//
//            try {
//
//                copyDataBase();
//
//            } catch (IOException e) {
//
//                throw new Error("Error copying database");
//
//            }
//        }
//
//    }
//
//    /**
//     * Check if the database already exist to avoid re-copying the file each time you open the application.
//     * @return true if it exists, false if it doesn't
//     */
//    public boolean checkDataBase(){
//
//        SQLiteDatabase checkDB = null;
//
//        try{
//            String myPath = DB_PATH + DB_NAME;
//            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
//
//        }catch(SQLiteException e){
//
//            //database does't exist yet.
//
//        }
//
//        if(checkDB != null){
//
//            checkDB.close();
//
//        }
//
//        return checkDB != null ? true : false;
//    }
//
//    /**
//     * Copies your database from your local assets-folder to the just created empty database in the
//     * system folder, from where it can be accessed and handled.
//     * This is done by transfering bytestream.
//     * */
//    private void copyDataBase() throws IOException{
//
//        //Open your local db as the input stream
//        InputStream myInput = mContext.getAssets().open(DB_NAME);
//
//        // Path to the just created empty db
//        String outFileName = DB_PATH + DB_NAME;
//
//        //Open the empty db as the output stream
//        OutputStream myOutput = new FileOutputStream(outFileName);
//
//        //transfer bytes from the inputfile to the outputfile
//        byte[] buffer = new byte[1024];
//        int length;
//        while ((length = myInput.read(buffer))>0){
//            myOutput.write(buffer, 0, length);
//        }
//
//        //Close the streams
//        myOutput.flush();
//        myOutput.close();
//        myInput.close();
//        Log.d("database", "database created successfully!!");
//    }
//
//    public void openDataBase() throws SQLException{
//        //Open the database
//        String myPath = DB_PATH + DB_NAME;
//        mDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
//    }
//
//    @Override
//    public synchronized void close() {
//        if(mDatabase != null)
//            mDatabase.close();
//        super.close();
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {}
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
//
//    // Add your public helper methods to access and get content from the database.
//    // You could return cursors by doing "return mDatabase.query(....)" so it'd be easy
//    // to you to create adapters for your views.
//
//    // get stores around a center point on map within a radius(in meters)
//    public Cursor getStoresNear(String productName, float centerLat, float centerLng , float radius ){
//
//        Cursor cursor = null;
//        /**
//         // initialize empty MergeCursor
//         MergeCursor mCursor = new MergeCursor(new Cursor[]{mDatabase.query(
//         TABLE_RETAILER,
//         new String[]{FIELD_RETAILER_NAME,FIELD_RETAILER_LAT,FIELD_RETAILER_LNG},
//         FIELD_RETAILER_NAME + "='NOT POSSIBLE'",null,null,null,null)});
//         ;
//         /**/
//        productName = productName.trim();
//        // if product is found in database
//        if (!productName.equals("")){
//            PointF center = new PointF(centerLat, centerLng);
//            final double mult = 1.1; // mult = 1.1; is more reliable
//            PointF p1 = calculateDerivedPosition(center, mult * radius, 0);
//            PointF p2 = calculateDerivedPosition(center, mult * radius, 90);
//            PointF p3 = calculateDerivedPosition(center, mult * radius, 180);
//            PointF p4 = calculateDerivedPosition(center, mult * radius, 270);
//
//            String table = TABLE_PRODUCTS + " INNER JOIN (" +
//                    TABLE_RETAILER + " INNER JOIN " + TABLE_PRODUCT_AVAIL + " ON " +
//                    TABLE_RETAILER + "." + FIELD_RETAILER_ID + "=" + TABLE_PRODUCT_AVAIL + "." + FIELD_PA_RETAIL_ID + ") AS temp ON " +
//                    TABLE_PRODUCTS + "." + FIELD_PRODUCT_ID + "=temp." + FIELD_PA_PRODUCT_ID;
//            String whereClause =
//                    FIELD_RETAILER_LAT + ">" + p3.x + " AND " +
//                            FIELD_RETAILER_LAT + "<" + p1.x + " AND " +
//                            FIELD_RETAILER_LNG + "<" + p2.y + " AND " +
//                            FIELD_RETAILER_LNG + ">" + p4.y + " AND " +
//                            TABLE_PRODUCTS + "." + FIELD_PRODUCT_NAME + " LIKE ?";
//            cursor = mDatabase.query(true,
//                    table,
//                    new String[]{FIELD_RETAILER_NAME, FIELD_RETAILER_LAT, FIELD_RETAILER_LNG},
//                    whereClause,
//                    new String[]{"%" + productName + "%"},
//                    null,null,null,null);
//        }
//        return cursor;
//    }
//
//
//
//    /**
//     * Calculates the end-point from a given source at a given range (meters)
//     * and bearing (degrees). This methods uses simple geometry equations to
//     * calculate the end-point.
//     *
//     * @param point
//     *            Point of origin
//     * @param range
//     *            Range in meters
//     * @param bearing
//     *            Bearing in degrees
//     * @return End-point from the source given the desired range and bearing.
//     */
//    private static PointF calculateDerivedPosition(PointF point, double range, double bearing)
//    {
//        double EarthRadius = 6371000; // m
//
//        double latA = Math.toRadians(point.x);
//        double lonA = Math.toRadians(point.y);
//        double angularDistance = range / EarthRadius;
//        double trueCourse = Math.toRadians(bearing);
//
//        double lat = Math.asin(
//                Math.sin(latA) * Math.cos(angularDistance) +
//                        Math.cos(latA) * Math.sin(angularDistance)
//                                * Math.cos(trueCourse));
//
//        double dlon = Math.atan2(
//                Math.sin(trueCourse) * Math.sin(angularDistance)
//                        * Math.cos(latA),
//                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));
//
//        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;
//
//        lat = Math.toDegrees(lat);
//        lon = Math.toDegrees(lon);
//
//        PointF newPoint = new PointF((float) lat, (float) lon);
//
//        return newPoint;
//
//    }
//}
