(
	~clearAnalysisDict = {|dict|
		dict.keysValuesDo{ |key, value|
			if(value.isKindOf(Buffer)){
				value.free;
			};
			dict.removeAt(key);
		};
		dict = ();
	};

	~getSlices = {|file, dict, threshold, chans|
		var selectedChans;
		dict.put(\filepath, file);
		
		selectedChans = case
			{chans == 0} {[chans]}
			{chans == 1} {[chans]}
			{chans >= 2} {(0..chans - 1)};

		dict.put(\chans, selectedChans);
		dict.put(\file, Buffer.readChannel(s, file, channels: selectedChans));
		dict.put(\indices, Buffer(s));
		//nrt onset slice of source
		FluidBufOnsetSlice.processBlocking(s, dict.at(\file), metric: 9, threshold: threshold, indices: dict.at(\indices), action: {"- found slices: ".postln});
	};

	~sortSlices = {|measure=\centroid, dict|
		//get
		var indices = dict.at(\indices);
		var file = dict.at(\file);
		var spec, stats, meanfeatures;
		//get and set new analysis buffers
		dict.put(\spec, Buffer(s));
		dict.put(\stats, Buffer(s));
		dict.put(\meanfeatures, Buffer(s));
		//vars
		spec = dict.at(\spec);
		stats = dict.at(\stats);
		meanfeatures = dict.at(\meanfeatures);
		//analysis
		indices.loadToFloatArray(action: {
			arg fa;
			//iterate through adjacent pairs of indices (tuple like)
			fa.doAdjacentPairs{
				arg start, end, i;
				var numSamps = end - start;
				// i.postln;
				//compute spectral features per fft frame (w selected feature)
				FluidBufSpectralShape.processBlocking(s, file, start, numSamps, features: spec, select:[measure], startChan: 0, numChans: 1);
				//buf stats channels: mean std skew kurtosis min median max
				FluidBufStats.processBlocking(s, spec, stats: stats, select:[\mean]);
				FluidBufCompose.processBlocking(s, stats, destination: meanfeatures, destStartFrame: i);
			};
			//get indices
			dict.put(\onsetArr, fa);
			dict.put(\size, fa.size);
			//get INDICES of sorted features
			meanfeatures.loadToFloatArray(action: { arg fa; dict.put(\sortedIndices, fa.order) });
			("- done analysis for file: " ++ dict.at(\filepath)).postln;
		});
	};

	~analyzeSlices = {|file, dict, thresh, metric, chans=0|
		var analysis;
		var path = (file++"thresh_"++thresh++"_metric_"++metric++"_chans_"++chans).asSymbol;
		var doesNotExist = Archive.at(path).isNil;

		if(doesNotExist) {
			~clearAnalysisDict.(dict);
			~getSlices.(file, dict, thresh, chans);
			if(metric != \ordered, {~sortSlices.(metric, dict)}, { "- slices in original order".postln; });

			"- writing cache".postln;
			~writeCache.(dict, path);

		} { 
			("- reading cache for file: " ++ file).postln;
			dict = ~readCache.(dict, path);
		};

		"\n";
	};

	~writeCache = {|dict, path|
		var dictFormatted = dict.copy;

		//remove actual loaded file
		dictFormatted.removeAt(\file);

		//buffers on server must be converted to arrays
		dictFormatted.keysValuesDo{|key, value|
			if(value.isKindOf(Buffer)){
				dictFormatted.at(key).loadToFloatArray(action: {|array|
					dictFormatted.put(key, array);
				});
			}
		};

		Archive.put(path, dictFormatted);
		Archive.write();
	};

	~readCache = {|dict, path|
		dict.putAll(Archive.at(path));

		dict.keysValuesDo{|key, value|
			//make the necessary arrays into buffers
			var featuresList = [\meanfeatures, \indices, \stats, \spec];
			if(featuresList.includes(key)){
				dict.put(key, Buffer.loadCollection(s, value));
			}
		};

		//load the actual file again
		dict.put(\file, Buffer.readChannel(s, dict.at(\filepath), channels: dict.at(\chans)));

		dict;
	};

	~getSlice = {|slice, dict|
		var sortedIndices = dict.at(\sortedIndices);
		var onsets = dict.at(\onsetArr);
		var startIdx = sortedIndices.wrapAt(slice);
		var endIdx = startIdx + 1;
		var startOnset = onsets.at(startIdx);
		var endOnset = onsets.at(endIdx);
		[startOnset, endOnset];
	};

	~pGetSlice = {|generator, dict|
		Pcollect ({ |i| ~getSlice.(i, dict).asRef; }, generator);
	};
);