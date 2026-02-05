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
~analyses = FluidDataSet(s);
~indices.loadToFloatArray(action:{
	arg fa;
	var spec = Buffer(s);
	var stats = Buffer(s);
	var stats2 = Buffer(s);
	var loudness = Buffer(s);
	var point = Buffer(s);

	fa.doAdjacentPairs{
		arg start, end, i;
		var num = end - start;

		FluidBufSpectralShape.processBlocking(s,~src,start,num,features:spec,select:[\centroid]);
		FluidBufStats.processBlocking(s,spec,stats:stats,select:[\mean]);

		FluidBufLoudness.processBlocking(s,~src,start,num,features:loudness,select:[\loudness]);
		FluidBufStats.processBlocking(s,loudness,stats:stats2,select:[\mean]);

		FluidBufCompose.processBlocking(s,stats,destination:point,destStartFrame:0);
		FluidBufCompose.processBlocking(s,stats2,destination:point,destStartFrame:1);

		~analyses.addPoint(i,point);
	};

	s.sync;

	~analyses.print;
});
)



//~meancentroids.sort;
//get indicies
~order = ~meancentroids.order;

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