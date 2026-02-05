(   
   DynGenDef(\mutant, 
        ~genFuncs ++ 
        "
        function pluck(in, freq) local(init, prev)
        (
            !init ? (prev = 0; init = 1);
            x = slide(in, 1, 100) * noise();
            coeff = exp(abs(freq) * (-2*$pi) / srate);
            x = linear_interp(x, prev, coeff);
            prev = x;
            x;
        );
        
        function string_resonator(in, freq, fb, damp, filter, q, topology, shape, verb_size, verb_mix) 
        local(init, del_ptr, prev, freq, damping_offset, delay_fract, delay_samp, ap_coeff, delay, read_pos, delay_rd, shaper, biquad, ap, lpf, out)
        (
            !init ? (prev = 0; del_ptr = 0; init = 1);

            freq = srate/abs(freq);
            damping_offset = (2*log(damp)) + freq;

            delay_fract = wrap(damping_offset, 0.3, 1.3);
            delay_samp = damping_offset - delay_fract;
            ap_coeff = (delay_fract - 1) / (delay_fract + 1);

            delay[del_ptr] = DCBlock(in + (prev * fb));
            read_pos = wrap(del_ptr - delay_samp, 0, srate);
            delay_rd = delay[read_pos];

            verb = schroeder(delay_rd, verb_size, 0.5);
            mixed = linear_interp(delay_rd, verb, verb_mix);
            delay_rd = DCBlock(scale(unit_arc(delay_rd, shape), 0, 1, 0, 1, 1));

            // shaper = DCBlock(unit_arc(delay_rd, shape));

            biquad = biquad(delay_rd, filter, q, topology);

            ap = allpass(biquad, ap_coeff);
            damp = lpf_op_simple(ap, 1-damp);
            prev = damp;
            out = damp;
            
            del_ptr = wrap(del_ptr + 1, 0, srate);

            out;
        );

        pitch_hz = in1; fb = in2; damp = in3; exciter_filter = in4;
        
        fuzz = in5; subharmonic = in6;
        
        filter = in7; q = in8; topology = in9;

        shaper = in10;

        verb_size = in11; verb_mix = in12;

        impulse = pluck(in0, exciter_filter);
        subharmonic_hz = pitch_hz / subharmonic;
        mod = sin(phasor(subharmonic_hz, 0) * 2*$pi) * (fuzz * subharmonic_hz);
        out0 = string_resonator(impulse, pitch_hz + mod, fb, damp, filter, q, topology, shaper, verb_size, verb_mix);
        "
    ).send;

    Ndef(\mutantString).clear;

    Ndef(\mutantString, {
        var in;
        var sig;
        var zerox;
        
        // in = Impulse.ar( \trigRate.ar(1) ) ! 2;
        in = InFeedback.ar(\inbus.kr(0), 1) ! 2;
        zerox = ZeroCrossing.ar(in);
        // zerox = Changed.ar(in.sign);
        zerox = Impulse.ar(zerox);

        sig = DynGenRT.ar(1, \mutant,
            //args
            in,
            \midiPitch.ar(50),
            \fb.ar(1),
            \damp.ar(1),
            \exciterFilter.ar(2000),

            \fuzz.ar(0),
            \subharmonic.ar(1),

            \filter.ar(500),
            // SinOsc.ar(0.1).exprange(100, 2000),
            \q.ar(1),
            \topology.ar(1),

            \shaper.ar(0),
            \verbSize.ar(1),
            \verbMix.ar(1)
        );

        sig = Disperser.ar(
            input: sig,
            freq: \dispFreq.kr(40),
            resonance: \dispResonance.kr(0.5),
            mix: \dispMix.kr(0.5),
            feedback: 1
        );

        sig = sig * \amp.kr(1).lag;
        sig = sig * \gain.kr(0).dbamp;

        sig.sanitize;
    });

    ~mutant = Bus.audio(s, 2);

    DynGenDef(\fmString, 
        ~genFuncs ++
        "
        function pluck(in, freq, attack, release) local(init, prev)
        (
            !init ? (prev = 0; init = 1);
            x = slide(in, attack, release) * noise();
            coeff = exp(abs(freq) * (-2*$pi) / srate);
            x = linear_interp(x, prev, coeff);
            prev = x;
            x;
        );
        
        function string_resonator(in, freq, fb, filter) local(init, del_ptr, prev1, prev2)(
            !init ? (
                prev1 = 0;
                prev2 = 0;
                del_ptr = 0;
                init = 1
            );

            freq = srate/abs(freq);
            coeff = exp(abs(filter) * (-2*$pi) / srate);
            delay_samp = (2*log(1-coeff)) + freq;

            lpf = linear_interp(prev1, prev2, 1-coeff);
            prev1 = lpf;

            delay[del_ptr] = DCBlock(in + lpf);
            read_pos = wrap(del_ptr - delay_samp, 0, srate);
            delay_rd = delay[read_pos];

            prev2 = delay_rd * fb;
            del_ptr = wrap(del_ptr + 1, 0, srate);

            delay_rd;
        );

        pitch_hz = in1;
        fb = in2;
        filter = in3;
        fuzz = in4;
        subharmonic = in5;
        exciter_filter = in6;
        exciter_attack = in7;
        exciter_release = in8;

        impulse = pluck(in0, exciter_filter, exciter_attack, exciter_release);
        subharmonic_hz = pitch_hz / subharmonic;
        mod = sin(phasor(subharmonic_hz, 0) * 2*$pi) * (fuzz * subharmonic_hz);
        out0 = string_resonator(impulse, pitch_hz + mod, fb, filter);
        "
    ).send;

    SynthDef(\fmString, {|gate=1|
        var sig = DynGenRT.ar(1, \fmString, 
            Impulse.ar(0) ! 2,
            Lag.ar(\midiPitch.ar(42.midicps()), \pitchLag.kr(0)),
            \fb.ar(-1),
            \filter.ar(2000),
            \fuzz.ar(0.01),
            \subharmonic.ar(1),
            \exciterFilter.ar(200),
            \exciterAttack.ar(1000),
            \exciterRelease.ar(2000),

        );
        
        sig = sig * EnvGen.ar(Env.perc(\atk.kr(0), \rel.kr(2), level: 1, curve: -2), gate, doneAction: 2);
        sig = sig * \amp.kr(1);
        sig = sig * \gain.kr(1).dbamp;

        Out.ar(\out.kr(0), sig.sanitize);
    }).add;
)