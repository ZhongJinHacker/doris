SELECT count() FROM github_events WHERE cast(v["type"] as string) = 'WatchEvent' AND cast(v["repo"]["name"] as string) IN ('apache/spark', 'GunZi200/Memory-Colour', 'isohuntto/openbay', 'wasabeef/awesome-android-ui') GROUP BY cast(v["payload"]["action"] as string)  