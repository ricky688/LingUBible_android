import assert from 'node:assert/strict';
let handler, count=0;
global.window={addEventListener:(_,f)=>handler=f};
global.grecaptcha={ready:f=>f(), execute:(key,args)=>{assert.equal(key,'public-key');assert.equal(args.action,'forgotPassword');count++;return Promise.resolve('verified-token')}};

                    window.addEventListener('message', function(e) {
                        if(e.data !== 'nonce-for-self-check' || e.ports.length !== 1 || window.nativeConnected) return;
                        window.nativeConnected=true;
                        var p=e.ports[0];
                        try { grecaptcha.ready(function(){ grecaptcha.execute('public-key',{action:'forgotPassword'}).then(function(t){p.postMessage(t)},function(){p.postMessage('ERROR')}) }); }
                        catch(e){p.postMessage('ERROR')}
                    });
                    
(async()=>{
 const received=[];const port={postMessage:v=>received.push(v)};
 handler({data:'wrong',ports:[port]});assert.equal(count,0);
 handler({data:'nonce-for-self-check',ports:[]});assert.equal(count,0);
 handler({data:'nonce-for-self-check',ports:[port]});await Promise.resolve();
 assert.equal(count,1);assert.deepEqual(received,['verified-token']);
 handler({data:'nonce-for-self-check',ports:[port]});assert.equal(count,1);
 console.log('PASS: nonce, single-port handshake, action and duplicate rejection');
})().catch(e=>{console.error(e);process.exitCode=1});
