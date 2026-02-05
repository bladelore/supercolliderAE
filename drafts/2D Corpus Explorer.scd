//pt1

~src = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
~src.play;

//live onset detection
(
{
	var sig = PlayBuf.ar(1, ~src, BufRateScale.ir(~src),loop:1);
	var slices = FluidOnsetSlice.ar(sig, 9, 0.5);
	[sig, slices]
}.play;
)

(
//make indices buffer
~indices = Buffer(s);
//nrt onset slice of source
FluidBufOnsetSlice.processBlocking(s, ~src, metric: 9, threshold: 0.1, indices:~indices, action: {"done".postln})
)

~indices.postln;

//sample position of indices
(
~indices.loadToFloatArray(action: {
	arg fa;
	fa.postln;
});
)

FluidWaveform(~src, ~indices);

(
~play_slice = {
	arg index;
	{
		var startsamp = Index.kr(~indices, index);
		var stopsamp = Index.kr(~indices, index + 1);
		var phs = Phasor.ar(0, BufRateScale.ir(~src), startsamp, stopsamp);
		var sig = BufRd.ar(1,~src,phs);
		var dursecs = (stopsamp - startsamp) / BufSampleRate.ir(~src);
		var env = EnvGen.kr(Env([0,1,1,0],[0.03,dursecs-0.06, 0.03]), doneAction: 2);
		sig.dup * env;
	}.play;
};
)


//pt 2
//write spectral analysis features
(
~indices.loadToFloatArray(action: {
	arg fa;
	var spec = Buffer(s);
	var stats = Buffer(s);
	var meancentroids = Buffer(s);
	var loudness = Buffer(s);
	//iterate through adjacent pairs of indices (tuple like)
	fa.doAdjacentPairs{
		arg start, end, i;
		var numSamps = end - start;
		start.postln;
		end.postln;
		i.postln;

		//compute spectral features per fft frame (centroid only)
		FluidBufSpectralShape.processBlocking(s, ~src, start, numSamps, features:spec, select:[\centroid]);
		//buf stats channels: mean std skew kurtosis min median max
		//select mean
		FluidBufStats.processBlocking(s, spec, stats: stats, select:[\mean]);
		FluidBufCompose.processBlocking(s, stats, destination: meancentroids, destStartFrame: i);
		FluidBufLoudness.processBlocking(s, ~src,start,numSamps,features:loudness,select:[\loudness]);

	};

	s.sync;

	meancentroids.loadToFloatArray(action: {
		arg fa;
		fa.postln;
		fa.size.postln;
		~meancentroids = fa;
	});
});
)

~meancentroids

//~meancentroids.sort;
//get indicies
~order = ~meancentroids.order;
~order
//play through slices lowest centroid to highest
(
fork{
	~order.do{
		arg i;
		"playing slice %".format(i).postln;
		~play_slice.(i);
		0.5.wait;
	};
}
)