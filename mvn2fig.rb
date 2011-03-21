require 'json'
#system('mvn dependency:tree -DoutputType=tgf -DoutputFile=graph.tgf')
File.open('graph.tgf') do |tgf|
  artifacts, edges = tgf.read.split("#\n")

  artifacts_by_id = {}
  artifacts.split("\n").each do |id_artifact|
    id, artifact = id_artifact.split(' ')
    artifact_comps = artifact.split(':')
    artifact_comps.pop if ['compile', 'test'].index(artifact_comps[-1])
    artifact_comps.delete('jar')
    artifacts_by_id[id] = artifact_comps
  end

  deps = Hash.new{|h,k| h[k] = Hash.new{|h,k| h[k] = []}}
  edges.split("\n").each do |from_to_scope|
    from, to, scope = from_to_scope.split(' ')
    deps[artifacts_by_id[from]][scope] << artifacts_by_id[to]
  end

  puts JSON.pretty_generate(deps)

  deps
end
