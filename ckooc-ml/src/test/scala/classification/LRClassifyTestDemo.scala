package classification

import ml.classification.LRClassifyUtils
import ml.feature.VectorizerUtils
import nlp.segment.SegmentUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Administrator on 2016/7/27.
  */
object LRClassifyTestDemo {
  def main(args: Array[String]) {
    Logger.getRootLogger.setLevel(Level.WARN)

    val conf = new SparkConf().setAppName("LRClassifyTest").setMaster("local[4]")
    val sc = new SparkContext(conf)

    val args = Array("ckooc-ml/data/news/test", "ckooc-ml/models/vectorize", "ckooc-ml/models/classification/lr")

    val dataPath = args(0)
    val vecModelPath = args(1)
    val lrModelPath = args(2)

    val classNum = 6


    //--- 分词
    val preUtils = SegmentUtils("ckooc-ml/src/main/resources/segment.properties")
    val lrUtils = new LRClassifyUtils(classNum)

    val (model, categoryMap) = lrUtils.load(sc, lrModelPath)

    val trainData = lrUtils.getFromDic(sc, dataPath, categoryMap)
    val splitedRDD = preUtils.run(trainData)


    //--- 向量化
    val minDocFreq = 2 //最小文档频率阀值
    val toTFIDF = true //是否将TF转化为TF-IDF
    val vocabSize = 5000 //词汇表大小

    val vectorizer = new VectorizerUtils()
      .setMinDocFreq(minDocFreq)
      .setToTFIDF(toTFIDF)
      .setVocabSize(vocabSize)

    val (cvModel, idf) = vectorizer.load(vecModelPath)
    val vectorizedRDD = vectorizer.vectorize(splitedRDD, cvModel, idf)


    //--- 测试LR分类
    lrUtils.test(vectorizedRDD, model)

    sc.stop()
  }
}
