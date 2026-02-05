(
    SynthDef(\driftingSines_mono_test, {|gate=1|
        var sig;
        var lfo = LFTri.ar(\lfoFreq.kr(0.01), 1).linlin(-1, 1, 0, 1);
        var freq = \freq.kr(404);
        var freqs = ([freq, freq/2, freq/5, freq/7] + LFNoise2.kr(0.1 ! 4, \pitchDev.kr(15)));
        var amps = ([0.4, 0.3, 0.2, 0.5] + LFNoise2.kr(0.1 ! 4, 0.1));
        var q = 0.8;
        var env = EnvGen.kr(Env.adsr(0, 0, 1, 1), gate, doneAction: 2);
    
        sig = Blip.ar(freqs, \numHarm.kr(0)) * amps;
        sig = BLowPass4.ar(sig, (5000 * lfo), q);
        sig = Splay.ar(sig);
        sig = LeakDC.ar(sig).sanitize;
    
        sig = sig * 0.25 * env;
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);
    }).add;
)


SynthDescLib.global[\default].def.dump

(

~suspendMono = {|sp_context, patt, releaseTime=0|
    var releaseGroup = patt.asStream.next(()).asDict.at(\group).postln;
    if(releaseGroup.isNil){"no release group set!".postln}{
        releaseGroup.release(releaseTime);
        sp_context.suspend(patt);
    };
};
)

(

    Pspawner({| sp |
        var mono;
        \a.postln;
        ~releaseNode = Group();
        mono = sp.par(
            Pdef(\lala,
            Pmono(\driftingSines_mono_test,
                \amp, 0.5,
                \freq, 440,
                \dur, 100,
                \group, ~releaseNode
            );
            )
        );

        sp.wait(2);
        ~suspendMono.(sp, mono, 1);
        // ~releaseNode.release(5);
        // sp.suspendsp(mono);
        
        \b.postln;
        mono = sp.par(
            Pdef(\lala,
            Pmono(\driftingSines_mono_test,
                \amp, 0.5,
                \freq, 500,
                \dur, 100,
                \group, ~releaseNode
            );
            )
        );

        // sp.wait(8);
        // sp.suspendAll();
    }).play(t)
)


(
    ~suspendMono = {|sp_context, patt, releaseTime=0|
        var releaseGroup = patt.asStream.next(()).asDict.at(\group).postln;
        if(releaseGroup.isNil){"no release group set!".postln}{
            releaseGroup.release(releaseTime);
            sp_context.suspend(patt);
        };
        patt;
    };

    
        Pspawner({| sp |
            var mono;
            \a.postln;
            ~releaseNode = Group();
            mono = sp.par(
                Pmono(\driftingSines_mono_test,
                    \amp, 0.5,
                    \freq, 440,
                    \dur, 100,
                    \group, ~releaseNode
                );
            );
    
            sp.wait(1);
            ~suspendMono.(sp, mono, 4);
            // ~releaseNode.release(5);
            // sp.suspendsp(mono);
            
            \b.postln;
            mono = sp.par(
                Pmono(\driftingSines_mono_test,
                    \amp, 0.5,
                    \freq, 500,
                    \dur, 100,
                    \group, ~releaseNode
                );
            );
        }).play(t)
    )

(
    Pbindef(\x);

    Pdef(\y, Pmono(\default, \dummy, 0, \octave, 6) <> Pbindef(\x)).trace.play;
    
    Pbindef(\x, \degree, Pn(Pseries(-7, 2, 8), inf));
    
    // Pdef(\y).stop;
)

Pdef.clear;

Pdef(\x, Pbind(\octave, 6)).play

// Pmono(\default, \octave, 6).trace.play;\

~hello = '';
Pdef(\x, Pmono(\default, \octave, 6, \callback, { ~id })).play;
Pdef(\x, Pmono(\default, \octave, 6, \callback, { ~id; })).play;

Pdef(\x, Pbind(\octave, 6, \callback, { ~id })).asStream.next(()).asDict.at(\callback).play


~hello;
x = PbindProxy.new;
x.source = Pdef(\x);



x = ['degree', 'fifi'].add('yoyo')
x = [type: \set, callback: {1023}]

x.asDict.at(\callback).value

Pbind(*x).trace.play(t)

Pmono(\default, \octave, 6).asStream.nextN(3, ())

Psubdivide

~last=''
y=Pbind(\dur,3,\degree,Prand(Scale.minor.degrees,inf));
~stream2=y.collect({|event| ~last=event; }); // <-- this is equal to y.play but it also the latest event in a variable ~last
~last.play
stream

(
    Pdef(\x, Pbind(\octave, 6, \callback, { Pdefn(\fxid, ~id.debug("id")) }));
    // z=Pdef(\x).collect({|event| ~last=event; }).play;
    // ~last.postln;
    // Pbindef(\x, \ctranspose, 1);
    Pdef(\x).play;
)

{ ~eventTypes[~type].value(s; }

(
    x = Pmono(\default, \octave, 6, \dur, 0.1);

    x.asStream
        .next(Event.parentEvents)
        .at(\groupEvent)
        .at(\nodeID)
        .value
)

Pproto

Event.default

PbindProxy

Pbind

fluid