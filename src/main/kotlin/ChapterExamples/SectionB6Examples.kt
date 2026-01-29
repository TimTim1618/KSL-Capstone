package ChapterExamples


import ksl.utilities.distributions.ChiSquaredDistribution
import ksl.utilities.distributions.KolmogorovSmirnovDist
import ksl.utilities.distributions.Uniform
import ksl.utilities.io.KSLFileUtil
import ksl.utilities.statistic.Statistic
import ksl.utilities.statistic.U01Test
import org.jetbrains.letsPlot.commons.intern.math.ipow


fun main() {
    // select file:  u01data.txt
    val myFile = KSLFileUtil.chooseFile()
    if (myFile != null){
        val data = KSLFileUtil.scanToArray(myFile.toPath())
        var k = 10 // set the number of intervals
        val chiSquaredTestStatistic = U01Test.chiSquaredTestStatistic(data, k)
        var chiDist = ChiSquaredDistribution(k - 1.0)
        var pValue = chiDist.complementaryCDF(chiSquaredTestStatistic)

        println("Example B.3")
        println("1-D Chi-Squared Test Statistic = $chiSquaredTestStatistic")
        println("P-Value = $pValue")
        println()

        k = 4
        val chi2D = U01Test.chiSquaredSerial2DTestStatistic(data, k)
        val dof = k.ipow(2) - 1
        chiDist = ChiSquaredDistribution(dof)
        pValue = chiDist.complementaryCDF(chi2D)
        println("Example B.4")
        println("2-D Chi-Squared Test Statistic = $chi2D")
        println("dof = $dof")
        println("P-Value = $pValue")
        println()

        val ks = Statistic.ksTestStatistic(data, Uniform())
        pValue = KolmogorovSmirnovDist.complementaryCDF(data.size, ks)
        println("Example B.5")
        println("K-S Test Statistic = $ks")
        println("P-Value = $pValue")
        println()
    }

}