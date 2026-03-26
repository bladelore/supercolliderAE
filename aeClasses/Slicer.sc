Slicer {
	classvar <instances;
	classvar <s;

	var <name, <dict;
	var <file, <threshold, <metric, <chans;

	*initClass {
		instances = IdentityDictionary.new;
		s = Server.default;
	}

	*new { |name, file, threshold=0.3, metric=\centroid, chans=0|
		var existing = instances.at(name);
		if(existing.notNil) {
			if(existing.file == file && existing.threshold == threshold && existing.metric == metric && existing.chans == chans) {
				^existing
			} {
				existing.init(name, file, threshold, metric, chans);
				^existing
			};
		};
		^super.new.init(name, file, threshold, metric, chans);
	}

	init { |argName, argFile, argThreshold, argMetric, argChans|
		name      = argName;
		file      = argFile;
		threshold = argThreshold;
		metric    = argMetric;
		chans     = argChans;
		dict      = ();

		instances.put(name, this);

		this.analyze;
	}

	analyze {
		var path = (file ++ "thresh_" ++ threshold ++ "_metric_" ++ metric ++ "_chans_" ++ chans).asSymbol;
		var doesNotExist = Archive.at(path).isNil;

		if(doesNotExist) {
			this.clearDict;
			this.getSlices;
			if(metric != \ordered,
				{ this.sortSlices },
				{ "- slices in original order".postln }
			);
			"- writing cache".postln;
			this.writeCache(path);
		} {
			("- reading cache for file: " ++ file).postln;
			this.readCache(path);
		};
	}

	clearDict {
		dict.keysValuesDo { |key, value|
			if(value.isKindOf(Buffer)) { value.free };
			dict.removeAt(key);
		};
		dict = ();
	}

	getSlices {
		var selectedChans = case
			{ chans == 0 } { [chans] }
			{ chans == 1 } { [chans] }
			{ chans >= 2 } { (0..chans - 1) };

		dict.put(\filepath,  file);
		dict.put(\chans,     selectedChans);
		dict.put(\file,      Buffer.readChannel(s, file, channels: selectedChans));
		dict.put(\indices,   Buffer(s));

		FluidBufOnsetSlice.processBlocking(s,
			dict.at(\file),
			metric:    9,
			threshold: threshold,
			indices:   dict.at(\indices),
			action:    { "- found slices: ".postln }
		);
	}

	sortSlices {
		var indices      = dict.at(\indices);
		var srcFile      = dict.at(\file);
		var spec, stats, meanfeatures;

		dict.put(\spec,         Buffer(s));
		dict.put(\stats,        Buffer(s));
		dict.put(\meanfeatures, Buffer(s));

		spec         = dict.at(\spec);
		stats        = dict.at(\stats);
		meanfeatures = dict.at(\meanfeatures);

		indices.loadToFloatArray(action: { |fa|
			fa.doAdjacentPairs { |start, end, i|
				var numSamps = end - start;
				FluidBufSpectralShape.processBlocking(s, srcFile, start, numSamps,
					features:  spec,
					select:    [metric],
					startChan: 0,
					numChans:  1
				);
				FluidBufStats.processBlocking(s, spec,
					stats:  stats,
					select: [\mean]
				);
				FluidBufCompose.processBlocking(s, stats,
					destination:    meanfeatures,
					destStartFrame: i
				);
			};

			dict.put(\onsetArr, fa);
			dict.put(\size,     fa.size);

			meanfeatures.loadToFloatArray(action: { |fa2|
				dict.put(\sortedIndices, fa2.order)
			});

			("- done analysis for file: " ++ dict.at(\filepath)).postln;
		});
	}

	writeCache { |path|
		var dictFormatted = dict.copy;
		dictFormatted.removeAt(\file);

		dictFormatted.keysValuesDo { |key, value|
			if(value.isKindOf(Buffer)) {
				dictFormatted.at(key).loadToFloatArray(action: { |array|
					dictFormatted.put(key, array);
				});
			}
		};

		Archive.put(path, dictFormatted);
		Archive.write();
	}

	readCache { |path|
		var featuresList = [\meanfeatures, \indices, \stats, \spec];

		dict.putAll(Archive.at(path));

		dict.keysValuesDo { |key, value|
			if(featuresList.includes(key)) {
				dict.put(key, Buffer.loadCollection(s, value));
			}
		};

		dict.put(\file,
			Buffer.readChannel(s, dict.at(\filepath), channels: dict.at(\chans))
		);
	}

	getSlice { |slice|
		var sortedIndices = dict.at(\sortedIndices);
		var onsets        = dict.at(\onsetArr);
		var startIdx      = sortedIndices.wrapAt(slice);
		var endIdx        = startIdx + 1;
		^[onsets.at(startIdx), onsets.at(endIdx)];
	}

	pGetSlice { |generator|
		^Pcollect({ |i| this.getSlice(i).asRef }, generator);
	}

	size   { ^dict.at(\size) }
	buffer { ^dict.at(\file) }

	printOn { |stream|
		stream << "Slicer(" << name
		        << ", file: "      << file
		        << ", threshold: " << threshold
		        << ", metric: "    << metric
		        << ", chans: "     << chans << ")";
	}
}