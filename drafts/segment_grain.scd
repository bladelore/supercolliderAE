//pt1
(
var file = "/Users/asherelazary/Desktop/Samples etc./Film Sounds/102060__juskiddink__finnish-sauna.wav";
//var file ="/Users/asherelazary/Desktop/Samples etc./NEW sample lib/207241__matxuketeam__hit-metalic-bin.wav";
//analyse mono
~src = Buffer.readChannel(s, file, channels: [0]);
//play stereo or source
~playSrc = Buffer.read(s, file);
//make indices buffer
~indices = Buffer(s);
~meanfeatures = Buffer(s);
~spec = Buffer(s);
~stats = Buffer(s);
//nrt onset slice of source
FluidBufOnsetSlice.processBlocking(s, ~src, metric: 9, threshold: 0.5, indices: ~indices, action: {"found slices".postln});
//FluidBufNoveltySlice.processBlocking(s, ~src, indices: ~indices, action: {"found slices".postln});
FluidWaveform(~src, ~indices);
)

(
~indices.loadToFloatArray(action: {
	arg fa;
	//iterate through adjacent pairs of indices (tuple like)
	fa.doAdjacentPairs{
		arg start, end, i;
		var numSamps = end - start;
		//compute spectral features per fft frame (centroid only)
		FluidBufSpectralShape.processBlocking(s, ~src, start, numSamps, features: ~spec, select:[\centroid]);
		//buf stats channels: mean std skew kurtosis min median max
		//select mean
		FluidBufStats.processBlocking(s, ~spec, stats: ~stats, select:[\mean]);
		FluidBufCompose.processBlocking(s, ~stats, destination: ~meanfeatures, destStartFrame: i);
	};
	//get indices, convert from float array
	~onsetArr = Array.newClear(fa.size);
	fa.do{ arg val, i; ~onsetArr[i] = val;};
	//get selected features
	~meanfeatures.loadToFloatArray(action: { arg fa; ~meanfeatures = fa;});
	//sort selected feature
	~featuresSorted = ~meanfeatures.order;
	"done analysis".postln;
});
)

//rebuild synthdef on reload sample
(
SynthDef(\segPlayer, {
	//slice index
	var this_idx = Select.kr(\slice.kr(0), ~featuresSorted);
	var startsamp = Select.kr(this_idx, ~onsetArr);
	var endsamp = Select.kr(this_idx + 1, ~onsetArr);
	//channels and sampler
	var numChans = ~playSrc.numChannels;
	var sig = PlayBuf.ar(numChans, ~playSrc, BufRateScale.ir(~playSrc) * \rate.kr(1), 0, startsamp, loop: 0);
	//handle channels
	var panned = case
	{numChans == 1} {Pan2.ar(sig, \pan.kr(0))}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], \pan.kr(0))}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], \pan.kr(0)); } ;
	//envelope
	var dursecs = (endsamp - startsamp) / BufSampleRate.ir(~playSrc);
	var env = Env.perc(\atk.kr(0.01), max((\dec.kr(0.2) * dursecs), 0.1), curve: -4).ar(Done.freeSelf);
	//out
	sig = panned * env * \gain.ar(0).dbamp;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\grainSlicer, { |bufnum, gate=1|
	var polarity, polarityProb, pan, grains;
	var bufFrames = BufFrames.ir(bufnum);
	var t = Impulse.ar( \trigRate.kr(100));
	var dur = \trigRate.kr(100).reciprocal * \overlap.kr(1) * \rate.kr(1).reciprocal;

	var this_idx = Select.kr(\slice.kr(0), ~featuresSorted);
	var startsamp = (Select.kr(this_idx, ~onsetArr) / bufFrames);
	var endsamp = (Select.kr(this_idx + 1, ~onsetArr) / bufFrames);
	var sampsDur = endsamp - startsamp;

	var granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(bufnum)) * \posRate.kr(0.5),
		start: startsamp,
		end: endsamp,
		trig: gate,
		resetPos: startsamp
	);

	granPhasor = Wrap.ar(granPhasor + (\pos.kr(0) * sampsDur), startsamp, endsamp);

	polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(1), inf), 1], inf) * -1;
	polarityProb = (polarityProb * 2 - 1);
	polarity = Demand.ar(t, 0, polarityProb);

	pan = Demand.ar(t, 0, Drand([-1, 0, 1] * \panMod.kr(1), inf));

	grains = TGrains.ar(
		numChannels: 2,
		trigger: t,
		bufnum: bufnum,
		rate: \rate.kr(1),
		centerPos: (granPhasor * BufDur.kr(bufnum)) + (dur * 0.5001),
		dur: dur,
		pan: pan,
		amp: polarity,
		interp: 4
	);

	grains = grains * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(1), curve: 0), gate, doneAction: Done.freeSelf);
	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0));
	Out.ar(\out.kr(0), grains);

}).add;
)

//should be the same slice
Synth(\segPlayer, [\slice, 2])
Synth(\grainSlicer, [\bufnum, ~src.bufnum, \slice, 2, \overlap, 20, \posRate, 1, \trigRate, 1000])

)

t = TempoClock.new(160/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

(
Pdef(\seg_pattern,
	PmonoArtic(\grainSlicer) <>
	Pbind(
		//\instrument, \grainSlicer,
		\bufnum, ~src.bufnum,
		//lowest to highest
		\slice, Pn(Pseries(0, 1, ~onsetArr.size), inf).trace,
		//\dec, Prand([0.5, 0.25], inf),
		\dur, Prand([0.25], inf),
		\dec, Prand([0.5, 0.25], inf),
		\trigRate, Pwhite(30, 20000, inf),
		\posRate, Pwhite(0.01, 1, inf),
		\polarityMod, Pwhite(0, 1, inf),
		\overlap, Pwhite(0.01, 40, inf),
		\panMod, Pwhite(0, 1, inf),
		//\rate, Prand([0.5, 0.25, 1, 1.5], inf),
	)
);
)

Pdef(\seg_pattern).play(t, quant:[1,0]);
