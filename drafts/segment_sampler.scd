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
	//get indices
	~onsetArr = fa;
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
)

t = TempoClock.new(160/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

(
Pdef(\seg_pattern,
	Pbind(
		//random slice
		//\slice, Prand(~idxrange, inf),
		//lowest to highest
		\slice, Pn(Pseries(0, 1, ~onsetArr.size), inf).trace,
		//\dur, Prand([0.25, Rest(), 0.5], inf),
		\dur, Prand([0.25], inf),
		\instrument, \segPlayer
	)
);
)

Pdef(\seg_pattern).play(t, quant:[1,0]);
