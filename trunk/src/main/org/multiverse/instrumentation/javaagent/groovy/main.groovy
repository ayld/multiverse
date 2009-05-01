import org.multiverse.instrumentation.javaagent.groovy.dsl.TransformDsl

println "Starting phase a"
TransformDsl.run(new File("phasea.groovy").text)
println "Finished phase a"

println 'Starting phase b'
TransformDsl.run(new File("phaseb.groovy").text)
println 'Finished phase b'

println 'Starting phase c'
TransformDsl.run(new File("phasec.groovy").text)
println 'Finished phase c'

println 'Starting phase d'
TransformDsl.run(new File("phased.groovy").text)
println 'Finished phase d'

//TransformDsl.run(new File('addtracing.groovy').text)