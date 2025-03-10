/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.seatunnel.spark

import org.apache.seatunnel.common.config.CheckResult
import org.apache.seatunnel.shade.com.typesafe.config.{Config, ConfigFactory}
import org.apache.seatunnel.env.RuntimeEnv
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.lang
import scala.collection.JavaConversions._

class SparkEnvironment extends RuntimeEnv {

  private var sparkSession: SparkSession = _

  private var streamingContext: StreamingContext = _

  var config: Config = ConfigFactory.empty()

  override def setConfig(config: Config): Unit = this.config = config

  override def getConfig: Config = config

  override def checkConfig(): CheckResult = new CheckResult(true, "")

  override def prepare(prepareEnv: lang.Boolean): Unit = {
    val sparkConf = createSparkConf()
    sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
    createStreamingContext
  }

  private def createSparkConf(): SparkConf = {
    val sparkConf = new SparkConf()
    config
      .entrySet()
      .foreach(entry => {
        sparkConf.set(entry.getKey, String.valueOf(entry.getValue.unwrapped()))
      })

    sparkConf
  }

  private def createStreamingContext: StreamingContext = {
    val conf = sparkSession.sparkContext.getConf
    val duration = conf.getLong("spark.stream.batchDuration", 5)
    if (streamingContext == null) {
      streamingContext =
        new StreamingContext(sparkSession.sparkContext, Seconds(duration))
    }
    streamingContext
  }

  def getStreamingContext: StreamingContext = {
    streamingContext
  }

  def getSparkSession: SparkSession = {
    sparkSession
  }

}
