import UIKit
import RiveRuntime

@objc(RiveView)
class RiveView: UIView {
  private var riveView: RiveAnimationView?

  override init(frame: CGRect) {
    super.init(frame: frame)
    self.initializeRiveView()
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    self.initializeRiveView()
  }

  private func initializeRiveView() {
    riveView = RiveAnimationView()
    if let riveView = riveView {
      addSubview(riveView)
      riveView.translatesAutoresizingMaskIntoConstraints = false
      riveView.topAnchor.constraint(equalTo: topAnchor).isActive = true
      riveView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
      riveView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
      riveView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
    }
  }

  @objc func setResourceName(_ name: NSString) {
    if let riveView = riveView {
      riveView.load(resource: name as String)
    }
  }
}