# Order Process Simulator

The order process simulator is a short script that simulates a simple order process on an Oracle database.The simulator simulates the user behavior of 50 users, who simultaneously and independently execute different order processes.  The simulator is used to create an example redo log which can be used as input for the [redo-log-parser](https://github.com/fyndalf/redo-log-parser).
To run your own simulation you may need to change the variables for the database user, password, IP and port.

# Requirements

In order for the tool to run, a working installation of Java 11 is required, as well as Maven.

For development, we recommend using IntelliJ IDEA 2020.2.

# Redo Log Extraction

The simulator was tested with an Oracle DB 19c EE which can be downloaded [here](https://www.oracle.com/database/technologies). Other databases or versions have not been tested and may not be supported. 
To enable a log extraction, the archiving of redo logs must be activated. To access the database sqlplus was used.

```sql
shutdown immediate
startup mount
alter database archivelog;
alter database open;

alter database add supplemental log data;
```

Now the database records and archives the redo logs. So the simulation can be started.
To flush the current redo log to the disk the following command can be used:


```sql
alter system switch logfile;
```

To extract the redo log the built-in LogMiner is used:

```sql
-- Load archive log 
EXECUTE DBMS_LOGMNR.ADD_LOGFILE( -
   LOGFILENAME => '### Archived Redo Log Path###', -
   OPTIONS => DBMS_LOGMNR.NEW);

-- Start LogMiner
EXECUTE DBMS_LOGMNR.START_LOGMNR(OPTIONS => -
   DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG);
```

The redo log can now be read via LogMiner. The result may have to be formatted.
