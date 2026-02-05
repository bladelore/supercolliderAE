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
				// i.postln;
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
	
	SynthDef(\segPlayer, {|buf, slice = #[0, 1], oneshot=1|
		var bufFrames, sampsDur, offset, startsamp, endsamp, numChans, sig, pan, panned, env, posRate, line, phasor, phase;
		
		bufFrames = BufFrames.ir(buf);
		numChans = buf.numChannels;
	
		offset = \offset.kr(0);
		startsamp = slice[0] + offset;
		endsamp = slice[1] + offset;
		sampsDur = endsamp - startsamp;
	
		posRate = \posRate.kr(1);
	
		//select looped or oneshot NOTE: doneaction controlled by release
		line= Line.ar(
				start: startsamp,
				end: endsamp,
				dur: (sampsDur / s.sampleRate) * BufRateScale.kr(buf) * posRate.reciprocal
		);
	
		phasor = Phasor.ar(
			rate: (1 / bufFrames * BufRateScale.kr(buf)) * posRate,
			start: startsamp,
			end: endsamp,
			resetPos: startsamp
		);
		
		phase = Select.ar(oneshot, [phasor, line]);
		
		sig = BufRd.ar(numChans, buf, line, interpolation: 2);
	
		//handle channels
		pan = \pan.kr(0);
		panned = case
		{numChans == 1} {Pan2.ar(sig, pan)}
		{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
		{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
		
		//envelope
		env = EnvGen.ar(Env.perc(\atk.kr(0.01), \rel.kr(0.5), curve: \curve.kr(-4)), gate: \gate.kr(1), doneAction: 2);
		
		//out
		sig = panned * env * \gain.kr(0).dbamp;
		sig = sig * \amp.kr(1);
		Out.ar(\out.kr(0), sig);
	}).add;

)
(
	~sliceBuf = Dictionary();
	// eg centroid, spread, skewness, kurtosis, rolloff, flatness, crest
	~analyzeSlices.(file: Platform.resourceDir +/+ "sounds/a11wlk01.wav", dict: ~sliceBuf, thresh: 0.1, metric: \centroid);
)
	
(
		Pdef(\seg_pattern,
			Pbind(
				\instrument, \segPlayer,
				\amp, 1,
				\buf, ~sliceBuf.at(\file),
				//lowest to highest
				\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32), ~sliceBuf),
				\dur, 0.25,
				\oneshot, 0
			)
		).play(t);
	)
)