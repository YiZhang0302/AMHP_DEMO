extern crate bn;
extern crate rand;
use bn::AffineG1;
use bn::{pairing, AffineG2, Fr, Group, Gt, G1, G2};
fn main() {
    let rng = &mut rand::thread_rng();

    // Generate private keys
    let alice_sk = Fr::random(rng);
    let bob_sk = Fr::random(rng);
    let carol_sk = Fr::random(rng);
    println!("alice_sk:{:?}", Fr::one().into_u256());
    println!("bob_sk:{:?}", bob_sk.into_u256());

    // Generate public keys in G1 and G2
    let (alice_pk1, alice_pk2) = (G1::one() * alice_sk, G2::one() * alice_sk);
    let (bob_pk1, bob_pk2) = (G1::one() * bob_sk, G2::one() * bob_sk);
    let (carol_pk1, carol_pk2) = (G1::one() * carol_sk, G2::one() * carol_sk);

    // println!("{:?}", AffineG2::from_jacobian(alice_pk2).unwrap());

    let a = 1;
    // println!("G1:{:?}", alice_pk1.normalize());

    // Each party computes the shared secret
    let alice_ss = pairing(bob_pk1, carol_pk2).pow(alice_sk);

    let gt_one = Gt::one();
    // println!("{:?}", gt_one.c0());
    // let bob_ss = pairing(carol_pk1, alice_pk2).pow(bob_sk);
    // let carol_ss = pairing(alice_pk1, bob_pk2).pow(carol_sk);

    // assert!(alice_ss == bob_ss && bob_ss == carol_ss);
}
