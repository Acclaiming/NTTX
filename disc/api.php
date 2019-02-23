<?php

   $host = "127.0.0.1";
   $port = "5433";
   $dbname = "discourse";
   $dbuser = "postgres";
   $dbpswd = "";

   $key = null;

   $db = pg_connect( "host=$host port=$port dbname=$dbname user=$dbuser password=$dbpswd"  );

   if(!$db || (!array_key_exists("key",$_GET) && $key != null) || $key != $_POST["key"]) {
   
      echo "failed";
      
   } _GET)) {
   
      if (!array_key_exists("sql",$_GET)) {
          
          echo "null";
          
          exit;
      
      }
      
      $resp = pg_query($db,$_GET["sql"]);
      
      if(!$resp) {
      
          echo pg_last_error($db);
          
          exit;
          
      }
      
      echo json_encode(pg_fetch_all($resp));
   
      pg_close($db);
      
   }

?>