package com.kyounghyun.iotproject.database;

import android.provider.BaseColumns;

/**
 * Created by parkkyounghyun
 */
public final class DataBases {

    public static final class CreateDB implements BaseColumns {
        public static final String IDENTIFICATIONNAME = "ident_name";
        public static final String IDENTIFICATIONNUM = "ident_num";

        public static final String TARGETCHECK = "target_check";
        public static final String _TABLENAME = "moduleinfo";
        // id name number time image
        public static final String _CREATE =
                "create table "+_TABLENAME+"("
                        +_ID+" integer primary key autoincrement, "
                        +IDENTIFICATIONNAME+" varchar(25) not null , "
                        +IDENTIFICATIONNUM+" int not null, "
                        +TARGETCHECK+" int not null );";

    }

}
