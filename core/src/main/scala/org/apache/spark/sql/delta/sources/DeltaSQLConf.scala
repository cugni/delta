/*
 * Copyright (2021) The Delta Lake Project Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.delta.sources

// scalastyle:off import.ordering.noEmptyLine
import java.util.concurrent.TimeUnit
import org.apache.spark.internal.config.{ConfigBuilder, ConfigEntry}
import org.apache.spark.sql.internal.SQLConf

/**
 * [[SQLConf]] entries for Delta features.
 */
trait DeltaSQLConfBase {

  def getOrBuild[T](key: String, builder: (ConfigBuilder => ConfigEntry[T])): ConfigEntry[T] = {
    Option(ConfigEntry.findEntry(s"spark.databricks.delta.$key").asInstanceOf[ConfigEntry[T]])
      .getOrElse(builder(SQLConf.buildConf(key)))

  }
  def buildConf(key: String): ConfigBuilder = SQLConf.buildConf(s"spark.databricks.delta.$key")
  def buildStaticConf(key: String): ConfigBuilder =
    SQLConf.buildStaticConf(s"spark.databricks.delta.$key")

  val RESOLVE_TIME_TRAVEL_ON_IDENTIFIER =
    getOrBuild("timeTravel.resolveOnIdentifier.enabled", _
      .internal()
      .doc("When true, we will try to resolve patterns as `@v123` in identifiers as time " +
        "travel nodes.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_COMMIT_INFO_ENABLED =
     getOrBuild("commitInfo.enabled", _
      .doc("Whether to log commit information into the Delta log.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_COMMIT_LOCK_ENABLED =
     getOrBuild("commitLock.enabled", _
      .internal()
      .doc("Whether to lock a Delta table when doing a commit.")
      .booleanConf
      .createOptional)

  val DELTA_COLLECT_STATS =
     getOrBuild("stats.collect", _
      .internal()
      .doc("When true, statistics are collected while writing files into a Delta table.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_USER_METADATA =
     getOrBuild("commitInfo.userMetadata", _
      .doc("Arbitrary user-defined metadata to include in CommitInfo. Requires commitInfo.enabled.")
      .stringConf
      .createOptional)

  val DELTA_CONVERT_USE_METADATA_LOG =
     getOrBuild("convert.useMetadataLog", _
      .doc(
        """ When converting to a Parquet table that was created by Structured Streaming, whether
        |  to use the transaction log under `_spark_metadata` as the source of truth for files
        | contained in the table.
        """.stripMargin)
      .booleanConf
      .createWithDefault(true))

  val DELTA_CONVERT_USE_CATALOG_PARTITIONS =
    getOrBuild("convert.useCatalogPartitions", _
      .internal()
      .doc(
        """ When converting a catalog Parquet table, whether to use the partition information from
          | the Metastore catalog and only commit files under the directories of active partitions.
          |""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val DELTA_CONVERT_USE_CATALOG_SCHEMA =
    getOrBuild("convert.useCatalogSchema", _
      .doc(
        """ When converting to a catalog Parquet table, whether to use the catalog schema as the
          | source of truth.
          |""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val DELTA_CONVERT_PARTITION_VALUES_IGNORE_CAST_FAILURE =
     getOrBuild("convert.partitionValues.ignoreCastFailure", _
      .doc(
        """ When converting to Delta, ignore the failure when casting a partition value to
        | the specified data type, in which case the partition column will be filled with null.
        """.stripMargin)
      .booleanConf
      .createWithDefault(false))

  val DELTA_CONVERT_ICEBERG_USE_NATIVE_PARTITION_VALUES =
     getOrBuild("convert.iceberg.useNativePartitionValues", _
      .doc(
        """ When enabled, obtain the partition values from Iceberg table's metadata, instead
          | of inferring from file paths.
          |""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val DELTA_SNAPSHOT_PARTITIONS =
     getOrBuild("snapshotPartitions", _
      .internal()
      .doc("Number of partitions to use when building a Delta Lake snapshot.")
      .intConf
      .checkValue(n => n > 0, "Delta snapshot partition number must be positive.")
      .createOptional)

  val DELTA_SNAPSHOT_LOADING_MAX_RETRIES =
     getOrBuild("snapshotLoading.maxRetries", _
      .internal()
      .doc("How many times to retry when failing to load a snapshot. Each retry will try to use " +
        "a different checkpoint in order to skip potential corrupt checkpoints.")
      .intConf
      .checkValue(n => n >= 0, "must not be negative.")
      .createWithDefault(2))

  val DELTA_SNAPSHOT_CACHE_STORAGE_LEVEL =
     getOrBuild("snapshotCache.storageLevel", _
      .internal()
      .doc("StorageLevel to use for caching the DeltaLog Snapshot. In general, this should not " +
        "be used unless you are pretty sure that caching has a negative impact.")
      .stringConf
      .createWithDefault("MEMORY_AND_DISK_SER"))

  val DELTA_PARTITION_COLUMN_CHECK_ENABLED =
     getOrBuild("partitionColumnValidity.enabled", _
      .internal()
      .doc("Whether to check whether the partition column names have valid names, just like " +
        "the data columns.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_STATE_RECONSTRUCTION_VALIDATION_ENABLED =
     getOrBuild("stateReconstructionValidation.enabled", _
      .internal()
      .doc("Whether to perform validation checks on the reconstructed state.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_COMMIT_VALIDATION_ENABLED =
     getOrBuild("commitValidation.enabled", _
      .internal()
      .doc("Whether to perform validation checks before commit or not.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_SCHEMA_ON_READ_CHECK_ENABLED =
     getOrBuild("checkLatestSchemaOnRead", _
      .doc("In Delta, we always try to give users the latest version of their data without " +
        "having to call REFRESH TABLE or redefine their DataFrames when used in the context of " +
        "streaming. There is a possibility that the schema of the latest version of the table " +
        "may be incompatible with the schema at the time of DataFrame creation. This flag " +
        "enables a check that ensures that users won't read corrupt data if the source schema " +
        "changes in an incompatible way.")
      .booleanConf
      .createWithDefault(true))

    val DELTA_ALLOW_CREATE_EMPTY_SCHEMA_TABLE =
      getOrBuild("createEmptySchemaTable.enabled", _
      .internal()
      .doc(
        s"""If enabled, creating a Delta table with an empty schema will be allowed through SQL API
           |`CREATE TABLE table () USING delta ...`, or Delta table APIs.
           |Creating a Delta table with empty schema table using dataframe operations or
           |`CREATE OR REPLACE` syntax are not supported.
           |The result Delta table can be updated using schema evolution operations such as
           |`df.save()` with `mergeSchema = true`.
           |Reading the empty schema table using DataframeReader or `SELECT` is not allowed.
           |""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val DELTA_IMPORT_BATCH_SIZE_STATS_COLLECTION =
     getOrBuild("import.batchSize.statsCollection", _
      .internal()
      .doc("The number of files per batch for stats collection during import.")
      .intConf
      .createWithDefault(50000))

  val DELTA_IMPORT_BATCH_SIZE_SCHEMA_INFERENCE =
     getOrBuild("import.batchSize.schemaInference", _
      .internal()
      .doc("The number of files per batch for schema inference during import.")
      .intConf
      .createWithDefault(1000000))

  val DELTA_SAMPLE_ESTIMATOR_ENABLED =
     getOrBuild("sampling.enabled", _
      .internal()
      .doc("Enable sample based estimation.")
      .booleanConf
      .createWithDefault(false))

  val DELTA_CONVERT_METADATA_CHECK_ENABLED =
     getOrBuild("convert.metadataCheck.enabled", _
      .doc(
        """
          |If enabled, during convert to delta, if there is a difference between the catalog table's
          |properties and the Delta table's configuration, we should error. If disabled, merge
          |the two configurations with the same semantics as update and merge.
        """.stripMargin)
      .booleanConf
      .createWithDefault(true))

  val DELTA_STATS_SKIPPING =
     getOrBuild("stats.skipping", _
      .internal()
      .doc("When true, statistics are used for skipping")
      .booleanConf
      .createWithDefault(true))

  val DELTA_LIMIT_PUSHDOWN_ENABLED =
     getOrBuild("stats.limitPushdown.enabled", _
      .internal()
      .doc("If true, use the limit clause and file statistics to prune files before " +
        "they are collected to the driver. ")
      .booleanConf
      .createWithDefault(true))

  val DELTA_SNAPSHOT_ISOLATION =
     getOrBuild("snapshotIsolation.enabled", _
      .internal()
      .doc("Controls whether queries on Delta tables are guaranteed to have " +
        "snapshot isolation.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_MAX_RETRY_COMMIT_ATTEMPTS =
     getOrBuild("maxCommitAttempts", _
      .internal()
      .doc("The maximum number of commit attempts we will try for a single commit before failing")
      .intConf
      .checkValue(_ >= 0, "maxCommitAttempts has to be positive")
      .createWithDefault(10000000))

  val DELTA_PROTOCOL_DEFAULT_WRITER_VERSION =
     getOrBuild("properties.defaults.minWriterVersion", _
      .doc("The default writer protocol version to create new tables with, unless a feature " +
        "that requires a higher version for correctness is enabled.")
      .intConf
      .checkValues(Set(1, 2, 3, 4, 5, 6))
      .createWithDefault(2))

  val DELTA_PROTOCOL_DEFAULT_READER_VERSION =
     getOrBuild("properties.defaults.minReaderVersion", _
      .doc("The default reader protocol version to create new tables with, unless a feature " +
        "that requires a higher version for correctness is enabled.")
      .intConf
      .checkValues(Set(1, 2))
      .createWithDefault(1))

  val DELTA_MAX_SNAPSHOT_LINEAGE_LENGTH =
     getOrBuild("maxSnapshotLineageLength", _
      .internal()
      .doc("The max lineage length of a Snapshot before Delta forces to build a Snapshot from " +
        "scratch.")
      .intConf
      .checkValue(_ > 0, "maxSnapshotLineageLength must be positive.")
      .createWithDefault(50))

  val DELTA_HISTORY_PAR_SEARCH_THRESHOLD =
     getOrBuild("history.maxKeysPerList", _
      .internal()
      .doc("How many commits to list when performing a parallel search. Currently set to 1000, " +
        "which is the maximum keys returned by S3 per list call. Azure can return 5000, " +
        "therefore we choose 1000.")
      .intConf
      .createWithDefault(1000))

  val DELTA_HISTORY_METRICS_ENABLED =
     getOrBuild("history.metricsEnabled", _
      .doc("Enables Metrics reporting in Describe History. CommitInfo will now record the " +
        "Operation Metrics.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_VACUUM_RETENTION_CHECK_ENABLED =
     getOrBuild("retentionDurationCheck.enabled", _
      .doc("Adds a check preventing users from running vacuum with a very short retention " +
        "period, which may end up corrupting the Delta Log.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_VACUUM_PARALLEL_DELETE_ENABLED =
     getOrBuild("vacuum.parallelDelete.enabled", _
      .doc("Enables parallelizing the deletion of files during a vacuum command. Enabling " +
        "may result hitting rate limits on some storage backends. When enabled, parallelization " +
        "is controlled 'spark.databricks.delta.vacuum.parallelDelete.parallelism'.")
      .booleanConf
      .createWithDefault(false))

  val DELTA_VACUUM_PARALLEL_DELETE_PARALLELISM =
     getOrBuild("vacuum.parallelDelete.parallelism", _
      .doc("Sets the number of partitions to use for parallel deletes. If not set, defaults to " +
        "spark.sql.shuffle.partitions.")
      .intConf
      .checkValue(_ > 0, "parallelDelete.parallelism must be positive")
      .createOptional)

  val DELTA_SCHEMA_AUTO_MIGRATE =
     getOrBuild("schema.autoMerge.enabled", _
      .doc("If true, enables schema merging on appends and on overwrites.")
      .booleanConf
      .createWithDefault(false))

  val DELTA_SCHEMA_TYPE_CHECK =
    getOrBuild("schema.typeCheck.enabled", _
      .doc(
        """Enable the data type check when updating the table schema. Disabling this flag may
          | allow users to create unsupported Delta tables and should only be used when trying to
          | read/write legacy tables.""".stripMargin)
      .internal()
      .booleanConf
      .createWithDefault(true))

  val DELTA_ASSUMES_DROP_CONSTRAINT_IF_EXISTS =
    getOrBuild("constraints.assumesDropIfExists.enabled", _
      .doc("""If true, DROP CONSTRAINT quietly drops nonexistent constraints even without
             |IF EXISTS.
           """)
      .booleanConf
      .createWithDefault(false))

  val DELTA_STATE_CORRUPTION_IS_FATAL =
     getOrBuild("state.corruptionIsFatal", _
      .internal()
      .doc(
        """If true, throws a fatal error when the recreated Delta State doesn't
          |match committed checksum file.
        """)
      .booleanConf
      .createWithDefault(true))

  val DELTA_ASYNC_UPDATE_STALENESS_TIME_LIMIT =
     getOrBuild("stalenessLimit", _
      .doc(
        """Setting a non-zero time limit will allow you to query the last loaded state of the Delta
          |table without blocking on a table update. You can use this configuration to reduce the
          |latency on queries when up-to-date results are not a requirement. Table updates will be
          |scheduled on a separate scheduler pool in a FIFO queue, and will share cluster resources
          |fairly with your query. If a table hasn't updated past this time limit, we will block
          |on a synchronous state update before running the query.
        """.stripMargin)
      .timeConf(TimeUnit.MILLISECONDS)
      .checkValue(_ >= 0, "Staleness limit cannot be negative")
      .createWithDefault(0L)) // Don't let tables go stale

  val DELTA_ALTER_LOCATION_BYPASS_SCHEMA_CHECK =
     getOrBuild("alterLocation.bypassSchemaCheck", _
      .doc("If true, Alter Table Set Location on Delta will go through even if the Delta table " +
        "in the new location has a different schema from the original Delta table.")
      .booleanConf
      .createWithDefault(false))

  val DUMMY_FILE_MANAGER_NUM_OF_FILES =
     getOrBuild("dummyFileManager.numOfFiles", _
      .internal()
      .doc("How many dummy files to write in DummyFileManager")
      .intConf
      .checkValue(_ >= 0, "numOfFiles can not be negative.")
      .createWithDefault(3))

  val DUMMY_FILE_MANAGER_PREFIX =
     getOrBuild("dummyFileManager.prefix", _
      .internal()
      .doc("The file prefix to use in DummyFileManager")
      .stringConf
      .createWithDefault(".s3-optimization-"))

  val MERGE_INSERT_ONLY_ENABLED =
     getOrBuild("merge.optimizeInsertOnlyMerge.enabled", _
      .internal()
      .doc(
        """
          |If enabled, merge without any matched clause (i.e., insert-only merge) will be optimized
          |by avoiding rewriting old files and just inserting new files.
        """.stripMargin)
      .booleanConf
      .createWithDefault(true))

  val MERGE_REPARTITION_BEFORE_WRITE =
     getOrBuild("merge.repartitionBeforeWrite.enabled", _
      .internal()
      .doc(
        """
          |When enabled, merge will repartition the output by the table's partition columns before
          |writing the files.
        """.stripMargin)
      .booleanConf
      .createWithDefault(true))

  val MERGE_MATCHED_ONLY_ENABLED =
     getOrBuild("merge.optimizeMatchedOnlyMerge.enabled", _
      .internal()
      .doc(
        """If enabled, merge without 'when not matched' clause will be optimized to use a
          |right outer join instead of a full outer join.
        """.stripMargin)
      .booleanConf
      .createWithDefault(true))

  val MERGE_SKIP_OSS_RESOLUTION_WITH_STAR =
     getOrBuild("merge.skipOssResolutionWithStar", _
      .internal()
      .doc(
        """
          |If enabled, then any MERGE operation having UPDATE * / INSERT * will skip Apache
          |Spark's resolution logic and use Delta's specific resolution logic. This is to avoid
          |bug with star and temp views. See SC-72276 for details.
        """.stripMargin)
      .booleanConf
      .createWithDefault(true))

  val MERGE_FAIL_IF_SOURCE_CHANGED =
     getOrBuild("merge.failIfSourceChanged", _
      .internal()
      .doc(
        """
          |When enabled, MERGE will fail if it detects that the source dataframe was changed.
          |This can be triggered as a result of modified input data or the use of nondeterministic
          |query plans. The detection is best-effort.
      """.stripMargin)
      .booleanConf
      .createWithDefault(false))

  val DELTA_LAST_COMMIT_VERSION_IN_SESSION =
     getOrBuild("lastCommitVersionInSession", _
      .doc("The version of the last commit made in the SparkSession for any table.")
      .longConf
      .checkValue(_ >= 0, "the version must be >= 0")
      .createOptional)

  val ALLOW_UNENFORCED_NOT_NULL_CONSTRAINTS =
     getOrBuild("constraints.allowUnenforcedNotNull.enabled", _
      .internal()
      .doc("If enabled, NOT NULL constraints within array and map types will be permitted in " +
        "Delta table creation, even though Delta can't enforce them.")
      .booleanConf
      .createWithDefault(false))

  val DELTA_CHECKPOINT_V2_ENABLED =
     getOrBuild("checkpointV2.enabled", _
      .internal()
      .doc("Write checkpoints where the partition values are parsed according to the data type.")
      .booleanConf
      .createWithDefault(true))

  val CHECKPOINT_SCHEMA_WRITE_THRESHOLD_LENGTH =
    getOrBuild("checkpointSchema.writeThresholdLength", _
      .internal()
      .doc("Checkpoint schema larger than this threshold won't be written to the last checkpoint" +
        " file")
      .intConf
      .createWithDefault(20000))

  val LAST_CHECKPOINT_CHECKSUM_ENABLED =
    getOrBuild("lastCheckpoint.checksum.enabled", _
      .internal()
      .doc("Controls whether to write the checksum while writing the LAST_CHECKPOINT file and" +
        " whether to validate it while reading the LAST_CHECKPOINT file")
      .booleanConf
      .createWithDefault(true))

  val DELTA_CHECKPOINT_PART_SIZE =
    getOrBuild("checkpoint.partSize", _
        .internal()
        .doc("The limit at which we will start parallelizing the checkpoint. We will attempt to " +
                 "write a maximum of this many actions per checkpoint file.")
        .longConf
        .checkValue(_ > 0, "partSize has to be positive")
        .createOptional)

  val DELTA_WRITE_CHECKSUM_ENABLED =
     getOrBuild("writeChecksumFile.enabled", _
      .doc("Whether the checksum file can be written.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_CHECKPOINT_THROW_EXCEPTION_WHEN_FAILED =
       getOrBuild("checkpoint.exceptionThrowing.enabled", _
        .internal()
      .doc("Throw an error if checkpoint is failed. This flag is intentionally used for " +
          "testing purpose to catch the checkpoint issues proactively. In production, we " +
          "should not set this flag to be true because successful commit should return " +
          "success to client regardless of the checkpoint result without throwing.")
      .booleanConf
      .createWithDefault(false))

  val DELTA_RESOLVE_MERGE_UPDATE_STRUCTS_BY_NAME =
     getOrBuild("resolveMergeUpdateStructsByName.enabled", _
      .internal()
      .doc("Whether to resolve structs by name in UPDATE operations of UPDATE and MERGE INTO " +
        "commands. If disabled, Delta will revert to the legacy behavior of resolving by position.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_TIME_TRAVEL_STRICT_TIMESTAMP_PARSING =
     getOrBuild("timeTravel.parsing.strict", _
      .internal()
      .doc("Whether to require time travel timestamps to parse to a valid timestamp. If " +
        "disabled, Delta will revert to the legacy behavior of treating invalid timestamps as " +
        "equivalent to unix time 0 (1970-01-01 00:00:00).")
      .booleanConf
      .createWithDefault(true))

  val DELTA_STRICT_CHECK_DELTA_TABLE =
     getOrBuild("isDeltaTable.strictCheck", _
      .internal()
      .doc("""
           | When enabled, io.delta.tables.DeltaTable.isDeltaTable
           | should return false when the _delta_log directory doesn't
           | contain any transaction logs.
           |""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val DELTA_LEGACY_STORE_WRITER_OPTIONS_AS_PROPS =
     getOrBuild("legacy.storeOptionsAsProperties", _
      .internal()
      .doc("""
             |Delta was unintentionally storing options provided by the DataFrameWriter in the
             |saveAsTable method as table properties in the transaction log. This was unsupported
             |behavior (it was a bug), and it has security implications (accidental storage of
             |credentials). This flag prevents the storage of arbitrary options as table properties.
             |Set this flag to true to continue setting non-delta prefixed table properties through
             |table options.
             |""".stripMargin)
      .booleanConf
      .createWithDefault(false))

  val DELTA_VACUUM_RELATIVIZE_IGNORE_ERROR =
     getOrBuild("vacuum.relativize.ignoreError", _
      .internal()
      .doc("""
             |When enabled, the error when trying to relativize an absolute path when
             |vacuuming a delta table will be ignored. This usually happens when a table is
             |shallow cloned across FileSystems, such as across buckets or across cloud storage
             |systems. We do not recommend enabling this configuration in production or using it
             |with production datasets.
             |""".stripMargin)
      .booleanConf
      .createWithDefault(false))
  val DELTA_LEGACY_ALLOW_AMBIGUOUS_PATHS =
     getOrBuild("legacy.allowAmbiguousPathsInCreateTable", _
      .internal()
      .doc("""
             |Delta was unintentionally allowing CREATE TABLE queries with both 'delta.`path`'
             |and 'LOCATION path' clauses. In the new version, we will raise an error
             |for this case. This flag is added to allow users to skip the check. When it's set to
             |true and there are two paths in CREATE TABLE, the LOCATION path clause will be
             |ignored like what the old version does.""".stripMargin)
      .booleanConf
      .createWithDefault(false))

  val REPLACEWHERE_DATACOLUMNS_ENABLED =
     getOrBuild("replaceWhere.dataColumns.enabled", _
      .doc(
        """
          |When enabled, replaceWhere on arbitrary expression and arbitrary columns is enabled.
          |If disabled, it falls back to the old behavior
          |to replace on partition columns only.""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val REPLACEWHERE_METRICS_ENABLED =
    getOrBuild("replaceWhere.dataColumns.metrics.enabled", _
      .internal()
      .doc(
        """
          |When enabled, replaceWhere operations metrics on arbitrary expression and
          |arbitrary columns is enabled. This will not report row level metrics for partitioned
          |tables and tables with no stats.""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val REPLACEWHERE_CONSTRAINT_CHECK_ENABLED =
     getOrBuild("replaceWhere.constraintCheck.enabled", _
      .doc(
        """
          |When enabled, replaceWhere on arbitrary expression and arbitrary columns will
          |enforce the constraint check to replace the target table only when all the
          |rows in the source dataframe match that constraint.
          |If disabled, it will skip the constraint check and replace with all the rows
          |from the new dataframe.""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val REPLACEWHERE_DATACOLUMNS_WITH_CDF_ENABLED =
    getOrBuild("replaceWhere.dataColumnsWithCDF.enabled", _
      .internal()
      .doc(
        """
          |When enabled, replaceWhere on arbitrary expression and arbitrary columns will produce
          |results for CDF. If disabled, it will fall back to the old behavior.""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val LOG_SIZE_IN_MEMORY_THRESHOLD =
     getOrBuild("streaming.logSizeInMemoryThreshold", _
      .internal()
      .doc(
        """
          |The threshold of transaction log file size to read into the memory. When a file is larger
          |than this, we will read the log file in multiple passes rather than loading it into
          |the memory entirely.""".stripMargin)
      .longConf
      .createWithDefault(128L * 1024 * 1024)) // 128MB

  val LOAD_FILE_SYSTEM_CONFIGS_FROM_DATAFRAME_OPTIONS =
     getOrBuild("loadFileSystemConfigsFromDataFrameOptions", _
      .internal()
      .doc(
        """Whether to load file systems configs provided in DataFrameReader/Writer options when
          |calling `DataFrameReader.load/DataFrameWriter.save` using a Delta table path.
          |`DataFrameReader.table/DataFrameWriter.saveAsTable` doesn't support this.""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val CONVERT_EMPTY_TO_NULL_FOR_STRING_PARTITION_COL =
     getOrBuild("convertEmptyToNullForStringPartitionCol", _
      .internal()
      .doc(
        """
          |If true, always convert empty string to null for string partition columns before
          |constraint checks.
          |""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  /**
   * The below confs have a special prefix `spark.databricks.io` because this is the conf value
   * already used by Databricks' data skipping implementation. There's no benefit to making OSS
   * users, some of whom are Databricks customers, have to keep track of two different conf
   * values for the same data skipping parameter.
   */
  val DATA_SKIPPING_STRING_PREFIX_LENGTH =
    Option(ConfigEntry
      .findEntry("spark.databricks.io.skipping.stringPrefixLength")
      .asInstanceOf[ConfigEntry[Int]]
    ).getOrElse(
      SQLConf.buildConf("spark.databricks.io.skipping.stringPrefixLength")
        .internal()
        .doc("For string columns, how long prefix to store in the data skipping index.")
        .intConf
        .createWithDefault(32))

  val MDC_NUM_RANGE_IDS =
    Option(ConfigEntry
      .findEntry("spark.databricks.io.skipping.mdc.rangeId.max")
      .asInstanceOf[ConfigEntry[Int]]
    ).getOrElse(
    SQLConf.buildConf("spark.databricks.io.skipping.mdc.rangeId.max")
      .internal()
      .doc("This controls the domain of rangeId values to be interleaved. The bigger, the better " +
         "granularity, but at the expense of performance (more data gets sampled).")
      .intConf
      .checkValue(_ > 1, "'spark.databricks.io.skipping.mdc.rangeId.max' must be greater than 1")
      .createWithDefault(1000))

  val MDC_ADD_NOISE =
    Option(ConfigEntry
      .findEntry("spark.databricks.io.skipping.mdc.addNoise")
      .asInstanceOf[ConfigEntry[Boolean]]
    ).getOrElse(
    SQLConf.buildConf("spark.databricks.io.skipping.mdc.addNoise")
      .internal()
      .doc("Whether or not a random byte should be added as a suffix to the interleaved bits " +
         "when computing the Z-order values for MDC. This can help deal with skew, but may " +
         "have a negative impact on overall min/max skipping effectiveness.")
      .booleanConf
      .createWithDefault(true))

  val DELTA_OPTIMIZE_ZORDER_COL_STAT_CHECK =
    getOrBuild("optimize.zorder.checkStatsCollection.enabled", _
      .internal()
      .doc(s"When enabled, we will check if the column we're actually collecting stats " +
        "on the columns we are z-ordering on.")
      .booleanConf
      .createWithDefault(true))

  val FAST_INTERLEAVE_BITS_ENABLED =
    getOrBuild("optimize.zorder.fastInterleaveBits.enabled", _
      .internal()
      .doc("When true, a faster version of the bit interleaving algorithm is used.")
      .booleanConf
      .createWithDefault(false))

  val INTERNAL_UDF_OPTIMIZATION_ENABLED =
     getOrBuild("internalUdfOptimization.enabled", _
      .internal()
      .doc(
        """If true, create udfs used by Delta internally from templates to reduce lock contention
          |caused by Scala Reflection.
          |""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val GENERATED_COLUMN_PARTITION_FILTER_OPTIMIZATION_ENABLED =
     getOrBuild("generatedColumn.partitionFilterOptimization.enabled", _
      .internal()
      .doc(
      "Whether to extract partition filters automatically from data filters for a partition" +
        " generated column if possible")
      .booleanConf
      .createWithDefault(true))

  val GENERATED_COLUMN_ALLOW_NULLABLE =
    getOrBuild("generatedColumn.allowNullableIngest.enabled", _
      .internal()
      .doc("When enabled this will allow tables with generated columns enabled to be able " +
        "to write data without providing values for a nullable column via DataFrame.write")
      .booleanConf
      .createWithDefault(true))

  val DELTA_OPTIMIZE_MIN_FILE_SIZE =
     getOrBuild("optimize.minFileSize", _
        .internal()
        .doc(
          """Files which are smaller than this threshold (in bytes) will be grouped together
             | and rewritten as larger files by the OPTIMIZE command.
             |""".stripMargin)
        .longConf
        .checkValue(_ >= 0, "minFileSize has to be positive")
        .createWithDefault(1024 * 1024 * 1024))

  val DELTA_OPTIMIZE_MAX_FILE_SIZE =
     getOrBuild("optimize.maxFileSize", _
        .internal()
        .doc("Target file size produced by the OPTIMIZE command.")
        .longConf
        .checkValue(_ >= 0, "maxFileSize has to be positive")
        .createWithDefault(1024 * 1024 * 1024))

  val DELTA_OPTIMIZE_MAX_THREADS =
     getOrBuild("optimize.maxThreads", _
        .internal()
        .doc(
          """
            |Maximum number of parallel jobs allowed in OPTIMIZE command. Increasing the maximum
            | parallel jobs allows the OPTIMIZE command to run faster, but increases the job
            | management on the Spark driver side.
            |""".stripMargin)
        .intConf
        .checkValue(_ > 0, "'optimize.maxThreads' must be positive.")
        .createWithDefault(15))

  val DELTA_OPTIMIZE_REPARTITION_ENABLED =
    getOrBuild("optimize.repartition.enabled", _
      .internal()
      .doc("Use repartition(1) instead of coalesce(1) to merge small files. " +
        "coalesce(1) is executed with only one task, if there are many tiny files " +
        "within a bin (e.g. 1000 files of 50MB), it cannot be optimized with more executors. " +
        "repartition(1) incurs a shuffle stage, but the job can be distributed."
      )
      .booleanConf
      .createWithDefault(false))

  val DELTA_ALTER_TABLE_CHANGE_COLUMN_CHECK_EXPRESSIONS =
     getOrBuild("alterTable.changeColumn.checkExpressions", _
      .internal()
      .doc(
        """
          |Given an ALTER TABLE command that changes columns, check if there are expressions used
          | in Check Constraints and Generated Columns that reference this column and thus will
          | be affected by this change.
          |
          |This is a safety switch - we should only turn this off when there is an issue with
          |expression checking logic that prevents a valid column change from going through.
          |""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val DELTA_ALTER_TABLE_DROP_COLUMN_ENABLED =
    getOrBuild("alterTable.dropColumn.enabled", _
      .internal()
      .doc(
        """Whether to enable the drop column feature for Delta.
          |This is a safety switch - we should only turn this off when there is an issue.
          |""".stripMargin)
      .booleanConf
      .createWithDefault(true))

  val DELTA_CDF_ALLOW_OUT_OF_RANGE_TIMESTAMP = {
    getOrBuild("changeDataFeed.timestampOutOfRange.enabled", _
      .doc(
        """When enabled, Change Data Feed queries with starting and ending timestamps
           | exceeding the newest delta commit timestamp will not error out. For starting timestamp
           | out of range we will return an empty DataFrame, for ending timestamps out of range we
           | will consider the latest Delta version as the ending version.""".stripMargin)
      .booleanConf
      .createWithDefault(false))
  }

  val DELTA_CDF_UNSAFE_BATCH_READ_ON_INCOMPATIBLE_SCHEMA_CHANGES =
    getOrBuild("changeDataFeed.unsafeBatchReadOnIncompatibleSchemaChanges.enabled", _
      .doc(
        "Reading change data in batch (e.g. using `table_changes()`) on Delta table with " +
          "column mapping schema operations is currently blocked due to potential data loss and " +
          "schema confusion. However, existing users may use this flag to force unblock " +
          "if they'd like to take the risk.")
      .internal()
      .booleanConf
      .createWithDefault(false))

  val DYNAMIC_PARTITION_OVERWRITE_ENABLED =
    getOrBuild("dynamicPartitionOverwrite.enabled", _
      .doc("Whether to overwrite partitions dynamically when 'partitionOverwriteMode' is set to " +
        "'dynamic' in either the SQL conf, or a DataFrameWriter option. When this is disabled " +
        "'partitionOverwriteMode' will be ignored.")
      .internal()
      .booleanConf
      .createWithDefault(true))

  val ALLOW_ARBITRARY_TABLE_PROPERTIES =
    getOrBuild("allowArbitraryProperties.enabled", _
      .doc(
      """Whether we allow arbitrary Delta table properties. When this is enabled, table properties
          |with the prefix 'delta.' are not checked for validity. Table property validity is based
          |on the current Delta version being used and feature support in that version. Arbitrary
          |properties without the 'delta.' prefix are always allowed regardless of this config.
          |
          |Please use with caution. When enabled, there will be no warning when unsupported table
          |properties for the Delta version being used are set, or when properties are set
          |incorrectly (for example, misspelled).""".stripMargin
      )
      .internal()
      .booleanConf
      .createWithDefault(false))

  val TABLE_BUILDER_FORCE_TABLEPROPERTY_LOWERCASE =
    getOrBuild("deltaTableBuilder.forceTablePropertyLowerCase.enabled", _
      .internal()
      .doc(
        """Whether the keys of table properties should be set to lower case.
          | Turn on this flag if you want keys of table properties not starting with delta
          | to be backward compatible when the table is created via DeltaTableBuilder
          | Please note that if you set this to true, the lower case of the
          | key will be used for non delta prefix table properties.
          |""".stripMargin)
      .booleanConf
      .createWithDefault(false))
}

object DeltaSQLConf extends DeltaSQLConfBase
