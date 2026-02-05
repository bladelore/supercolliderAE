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

~getSlices = {|file, dict, threshold|
	dict.put(\filepath, file);
	dict.put(\file, Buffer.readChannel(s, file, channels: [0]));
	dict.put(\indices, Buffer(s));
	//nrt onset slice of source
	FluidBufOnsetSlice.processBlocking(s, dict.at(\file), metric: 9, threshold: threshold, indices: dict.at(\indices), action: {"found slices".postln});
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
			//compute spectral features per fft frame (w selected feature)
			FluidBufSpectralShape.processBlocking(s, file, start, numSamps, features: spec, select:[measure]);
			//buf stats channels: mean std skew kurtosis min median max
			FluidBufStats.processBlocking(s, spec, stats: stats, select:[\mean]);
			FluidBufCompose.processBlocking(s, stats, destination: meanfeatures, destStartFrame: i);
		};
		//get indices
		dict.put(\onsetArr, fa);
		dict.put(\size, fa.size);
		//get INDICES of sorted features
		meanfeatures.loadToFloatArray(action: { arg fa; dict.put(\sortedIndices, fa.order) });
		"done analysis".postln;
	});

};

~analyzeSlices = {|file, dict, thresh, metric|
	~clearAnalysisDict.(dict);
	~getSlices.(file, dict, thresh);
	~sortSlices.(metric, dict);
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
)